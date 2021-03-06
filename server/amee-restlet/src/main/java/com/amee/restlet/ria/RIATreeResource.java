package com.amee.restlet.ria;

//@Component("riaTreeResource")
//@Scope("prototype")
//public class RIATreeResource extends AuthorizeResource {
//
//    @Autowired
//    private DataService dataService;
//
//    @Autowired
//    private PathItemService pathItemService;
//
//    private String node;
//    private PathItem pathItem;
//
//    @Override
//    public void initialise(Context context, Request request, Response response) {
//        super.initialise(context, request, response);
//        setRequireSuperUser(true);
//        node = request.getResourceRef().getQueryAsForm().getFirstValue("node");
//        if ((node == null) || node.equalsIgnoreCase("root")) {
//            pathItem = pathItemService.getPathItemGroup(getActiveEnvironment()).getRootPathItem();
//        } else if (UidGen.INSTANCE_12.isValid(node)) {
//            pathItem = pathItemService.getPathItemGroup(getActiveEnvironment()).findByUId(node);
//            if (!pathItem.getObjectType().equals(ObjectType.DC)) {
//                pathItem = null;
//            }
//        }
//    }
//
//    @Override
//    public List<IAMEEEntityReference> getEntities() {
//        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
//        entities.add(dataService.getDataCategoryByUid(pathItem.getUid()));
//        PathItem parent = pathItem.getParent();
//        while (parent != null) {
//            entities.add(dataService.getDataCategoryByUid(parent.getUid()));
//            parent = parent.getParent();
//        }
//        entities.add(getActiveEnvironment());
//        Collections.reverse(entities);
//        return entities;
//    }
//
//    @Override
//    public boolean isValid() {
//        return super.isValid() && (pathItem != null);
//    }
//
//    @Override
//    public String getTemplatePath() {
//        throw new UnsupportedOperationException("PermissionsResource does not have or need a template.");
//    }
//
//    @Override
//    public Representation represent(Variant variant) throws ResourceException {
//        try {
//            JSONObject obj;
//            JSONArray arr = new JSONArray();
//            for (PathItem child : pathItem.getChildrenByType("DC")) {
//                DataCategory dc = dataService.getDataCategoryByUid(child.getUid());
//                obj = new JSONObject();
//                obj.put("id", child.getUid());
//                obj.put("text", child.getName());
//                obj.put("leaf", child.getChildrenByType("DC").isEmpty());
//                obj.put("path", child.getPath());
//                obj.put("fullPath", child.getFullPath());
//                if (dc.getItemDefinition() != null) {
//                    obj.put("itemDefinitionUid", dc.getItemDefinition().getUid());
//                }
//                arr.put(obj);
//            }
//            return new JsonRepresentation(arr);
//        } catch (JSONException e) {
//            log.error("Caught JSONException: " + e.getMessage());
//            throw new RuntimeException("");
//        }
//    }
//}
