<#include '/dataCommon.ftl'>
<#include '/includes/before_content.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/data_service.js" type="text/javascript"></script>

<script type="text/javascript">

    function deleteDataCategory(dataCategoryUid, dataCategoryPath) {
        resourceUrl = dataCategoryPath + '?method=delete';
        resourceElem = $('Elem_' + dataCategoryUid);
        resourceType = 'Data Category';
        var deleteResource = new DeleteResource();
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }

    function deleteDataItem(uid, dataItemPath) {
        resourceUrl = dataItemPath + '?method=delete';
        resourceElem = $('Elem_' + uid);
        resourceType = 'Data Item';
        var deleteResource = new DeleteResource();
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }

    // create resource objects
    var DATA_ACTIONS = new ActionsResource({path: '/data/actions'});
    var ITEM_DEFINITIONS = new ItemDefinitionsResource();
    var dataCategoryApiService = new DataCategoryApiService({
        heading : "Data Items",
        headingElementName : "apiHeading",
        contentElementName : "apiContent",
        pagerTopElementName : 'apiTopPager',
        pagerBtmElementName : 'apiBottomPager',
        dataHeadingCategory : 'Data Category Details',
        dataHeadingCategoryElementName : 'apiDataCategoryHeading',
        dataContentElementName : "apiDataCategoryContent",
        apiVersion : '2.0',
        updateCategory: true,
        createCategory: true});

    // use resource loader to load resources and notify on loaded
    var resourceLoader = new ResourceLoader();
    resourceLoader.addResource(DATA_ACTIONS);
    resourceLoader.addResource(ITEM_DEFINITIONS);
    resourceLoader.observe('loaded', function() {
        dataCategoryApiService.start();
    });
    resourceLoader.start();

    document.observe('dom:loaded', function() {
        // hide n/a atom option
        $('showAPIATOM').style.visibility = "hidden";
    });

</script>

<h1>Data Category</h1>

<#include 'dataTrail.ftl'>

<h2 id="apiDataCategoryHeading"></h2>
<p id="apiDataCategoryContent"></p>
<h2 id="apiHeading"></h2>
<div id="apiTopPager"></div>
<table id="apiContent"></table>
<div id="apiBottomPager"></div>
<div id="apiUpdateDataCategory"></div>
<div id="apiUpdateSubmitStatus"></div><br/>
<div id="apiCreateDataCategory"></div>
<div id="apiCreateSubmitStatus"></div>

<#include '/includes/after_content.ftl'>