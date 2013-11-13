import groovy.json.JsonSlurper
import groovy.xml.XmlUtil
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.oltu.oauth2.client.OAuthClient
import org.apache.oltu.oauth2.client.URLConnectionClient
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest
import org.apache.oltu.oauth2.client.request.OAuthClientRequest
import org.apache.oltu.oauth2.client.response.GitHubTokenResponse
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse
import org.apache.oltu.oauth2.common.OAuth
import org.apache.oltu.oauth2.common.OAuthProviderType
import org.apache.oltu.oauth2.common.message.types.GrantType
import ratpack.session.Session
import ratpack.session.store.MapSessionsModule
import ratpack.session.store.SessionStorage

import static ratpack.groovy.Groovy.ratpack
import static ratpack.groovy.Groovy.groovyTemplate

def casUrl = "http://localhost:5252/cas"
def hostPortUrl = "http://localhost:5050"
def aClientId = "keyvalue"
def aClientSecret = "topsecret"

ratpack {
    modules {
        register(new MapSessionsModule(10, 5))
    }
    handlers {
        /*
        prefix "custom/resources/:owner", {
            handler {
                if (request.queryParams.code) {
                    def http = new HTTPBuilder()
                    def body = http.get(
                            uri: "${casUrl}/oauth2.0/accessToken",
                            query: [
                                    client_id: 'keyvalue',
                                    redirect_uri: "${hostPortUrl}/custom/resources/${allPathTokens.owner}",
                                    client_secret: clientSecret,
                                    code: request.queryParams.code
                            ]
                    )
                    def parsedBody = [:]
                    "${body}".split("&").each { line ->
                        def (key, value) = line.split("=")
                        parsedBody[key] = value
                    }
                    def body2 = http.get(
                            uri: "${casUrl}/oauth2.0/profile",
                            query: [
                                    access_token: parsedBody['access_token']
                            ]
                    )
                    get(SessionStorage).profile = body2
                }
                next()
            }
            handler {
                if (!get(SessionStorage).profile || !get(SessionStorage).profile.perms.contains("read")) {
                    def redirectUri = URLEncoder.encode("${hostPortUrl}/resources/${allPathTokens.owner}", "UTF-8")
                    render groovyTemplate(
                            "wronguser.html",
                            user: get(SessionStorage).profile?.id ?: "no one",
                            link: "${casUrl}/oauth2.0/authorize?client_id=${clientId}&redirect_uri=${redirectUri}"
                    )
                    return
                }
                next()
            }
        }
         */
        prefix "resources/:owner", {
            handler {
                if (request.queryParams.code) {
                    def orequest = OAuthClientRequest.tokenLocation("${casUrl}/oauth2.0/accessToken").with {
                        grantType = GrantType.AUTHORIZATION_CODE
                        clientId = aClientId
                        clientSecret = aClientSecret
                        redirectURI = "${hostPortUrl}/resources/${allPathTokens.owner}"
                        code = request.queryParams.code
                        buildQueryMessage()
                    }
                    def oclient = new OAuthClient(new URLConnectionClient())
                    def oresponse = oclient.accessToken(orequest, GitHubTokenResponse)
                    def oaccessToken = oresponse.accessToken
                    def bearerClientRequest = new OAuthBearerClientRequest("${casUrl}/oauth2.0/profile").with {
                        accessToken = oaccessToken
                        buildQueryMessage()
                    }

                    get(SessionStorage)."${allPathTokens.owner}" = new JsonSlurper().parseText(oclient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse).body)
                    def queryString = request.queryParams.findAll { it.key != "code" }.collect {"${it.key}=${it.value}"}.join('&')
                    redirect "${hostPortUrl}/resources/${allPathTokens.owner}${!!queryString || queryString == '' ? '' : "?${queryString}"}"
                    return
                }
                next()
            }

            handler {
                if (!get(SessionStorage)."${allPathTokens.owner}" || !get(SessionStorage)."${allPathTokens.owner}".perms.contains("read")) {
                    def orequest = OAuthClientRequest.authorizationLocation("${casUrl}/oauth2.0/authorize").with {
                        clientId = aClientId
                        redirectURI = "${hostPortUrl}/resources/${allPathTokens.owner}"
                        buildQueryMessage()
                    }
                    render groovyTemplate(
                            "wronguser.html",
                            user: get(SessionStorage).profile?.id ?: "no one",
                            link: orequest.locationUri
                    )
                    return
                }
                next()
            }
        }
        get("resources/:owner") {
            def profile = get(SessionStorage)."${allPathTokens.owner}"
            response.send "hello, ${profile.id}, you have the ${profile.perms} perms"
        }

        get("custom/resources/:owner") {
            def profile = get(SessionStorage).profile
            response.send "hello, ${profile.id}, you have the ${profile.perms} perms"
        }

        get("whoami") {
            response.send "you are: ${get(SessionStorage).remoteUser}"
        }

        get("logout") {
            def session = get(Session)
            session.terminate()
            response.send("session terminated")
        }

        assets "public"
    }
}
