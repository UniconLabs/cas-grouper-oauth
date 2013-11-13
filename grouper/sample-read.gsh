grouperSession = GrouperSession.startRootSession();

permissionDef = new AttributeDefSave(grouperSession).assignName("test:permissionDef").assignToEffMembership(true).assignToGroup(true).assignAttributeDefType(AttributeDefType.perm).assignCreateParentStemsIfNotExist(true).save();
permissionDef.attributeDefActionDelegate.configureActionList("read, write, readWrite, admin");

read = permissionDef.getAttributeDefActionDelegate().findAction("read", true);
write = permissionDef.getAttributeDefActionDelegate().findAction("write", true);
readWrite = permissionDef.getAttributeDefActionDelegate().findAction("readWrite", true);
admin = permissionDef.getAttributeDefActionDelegate().findAction("admin", true);

billsResource = new AttributeDefNameSave(grouperSession, permissionDef).assignName("test:httplocalhost5050resourcesbill").assignDisplayExtension("http://localhost:5050/resources/bill").save();

user = new GroupSave(grouperSession).assignName("test:user").assignTypeOfGroup(TypeOfGroup.role).save();

user.getPermissionRoleDelegate().assignRolePermission("read", billsResource, PermissionAllowed.ALLOWED);
