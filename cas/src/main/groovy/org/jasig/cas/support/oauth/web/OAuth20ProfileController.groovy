package org.jasig.cas.support.oauth.web

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import groovy.json.JsonSlurper
import groovy.json.StreamingJsonBuilder
import groovyx.net.http.HTTPBuilder
import org.apache.commons.lang3.StringUtils
import org.jasig.cas.support.oauth.OAuthConstants
import org.jasig.cas.support.oauth.profile.CasWrapperProfile
import org.jasig.cas.ticket.TicketGrantingTicket
import org.jasig.cas.ticket.registry.TicketRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.AbstractController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OAuth20ProfileController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(OAuth20ProfileController)
    private final TicketRegistry ticketRegistry

    private final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(new Config())

    public OAuth20ProfileController(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        def m = hazelcastInstance.getMap("testmap")
        def accessToken = request.getParameter(OAuthConstants.ACCESS_TOKEN)
        log.debug("accessToken : {}", accessToken);

        def jsonBuilder = new StreamingJsonBuilder(response.writer)
        response.contentType = "application/json"

        // accessToken is required
        if (StringUtils.isBlank(accessToken)) {
            log.error("missing accessToken")
            jsonBuilder.error OAuthConstants.MISSING_ACCESS_TOKEN
            response.flushBuffer()
            return null
        }

        println "accessToken: ${accessToken}"
        println "another: ${m.get(accessToken)}"
        def tgtId = m."${accessToken}"."tgt"


        def ticketGrantingTicket = (TicketGrantingTicket) ticketRegistry.getTicket(tgtId)
        if (ticketGrantingTicket == null || ticketGrantingTicket.expired) {
            log.error("expired accessToken : {}", accessToken)
            jsonBuilder.error OAuthConstants.EXPIRED_ACCESS_TOKEN
            response.flushBuffer()
            return null
        }

        def getPerms = { user, service ->
            def http = new HTTPBuilder('http://localhost:8080/grouper-ws/servicesRest/json/v2_1_5/')
            http.auth.basic('GrouperSystem', 'letmein7')
            http.encoder.'text/x-json' = http.encoder.'application/json'
            http.encoder.charset = "UTF-8"

            println "service: ${service}"
            def postBody = [
                    "WsRestGetPermissionAssignmentsLiteRequest": [
                            "actAsSubjectId": "GrouperSystem",
                            'wsSubjectId': user,
                            // "wsAttributeDefNameName": "test:httplocalhost5050resourcesbill"
                            "wsAttributeDefNameName": "test:${service.replaceAll(":", "").replaceAll("/", "")}".toString()
                    ]
            ]

            http.post(
                    path: 'permissionAssignments',
                    body: postBody,
                    requestContentType: "text/x-json"
            ) { resp ->
                def slurper = new JsonSlurper()
                def j = slurper.parse(new InputStreamReader(resp.entity.content, "UTF-8"))
                return j."WsGetPermissionAssignmentsResults"."wsPermissionAssigns".collect {it.action}
            }
        }


        def principal = ticketGrantingTicket.authentication.principal
        def pPerms = getPerms principal.id, m."${accessToken}"."serviceId"

        jsonBuilder {
            "${CasWrapperProfile.ID}" principal.id
            "${CasWrapperProfile.ATTRIBUTES}" principal.attributes.collect { key, value -> [key: value] }
            "perms" pPerms
        }

        return null
    }
}
