<#include 'profileCommon.ftl'>
<#include '/includes/furniture.ftl'>
<#include '/includes/before_content.ftl'>
<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/profile_service.js" type="text/javascript"></script>
<script type="text/javascript">
function profileCategoryLoaded() {
  <#if activeUser.apiVersion.versionOne>
    $("totalAmountPerMonth").innerHTML = this.resource.totalAmountPerMonth;
  <#else>
    $("tAmount").innerHTML = this.resource.tAmount;
  </#if>
}
function profileItemDeleted() {
  Effect.Fade(this.resourceElem);
  var profileCategoryResource = new ProfileCategoryResource('${profile.uid}', '${browser.pathItem.fullPath}');
  profileCategoryResource.loadedCallback = profileCategoryLoaded;
  profileCategoryResource.load();
}
function deleteProfileItem(profileItemUid, profileItemPath) {
  if (profileItemPath && profileItemPath.indexOf("/") > -1) {
    resourceUrl = profileItemPath + '&method=delete';
  } else {
    resourceUrl = profileItemPath + '?method=delete';
  }
  Log.debug(resourceUrl);
  resourceElem = $('Elem_' + profileItemUid);
  resourceType = 'Profile Item';
  var deleteResource = new DeleteResource()
  deleteResource.deleteResourceCallback = profileItemDeleted;
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>
<h1>Profile Category</h1>
<#include 'profileTrail.ftl'>
<#if activeUser.apiVersion.versionOne>
    <h2>Profile Category Details</h2>
    <p>Name: ${dataCategory.name}<br/>
       <#if dataCategory.path != ''>Path: ${dataCategory.path}<br/></#if>
       Full Path: ${browser.fullPath}<br/>
       <#if dataCategory.itemDefinition??>Item Definition: ${dataCategory.itemDefinition.name}<br/></#if>
       Environment: ${dataCategory.environment.name}<br/>
       Data Category UID: ${dataCategory.uid}<br/>
    </p>

    <#assign children = browser.pathItem.findChildrenByType('DC')>
    <#if 0 != children?size>
      <h2>Profile Categories</h2>
      <p>
      <table>
      <tr>
        <th>Path</th>
        <th>Actions</th>
      </tr>
      <#list children as pi>
        <tr>
          <td>${pi.name}</td>
          <td>
              <#if browser.profileActions.allowView><a href='${basePath}/${pi.path}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
          </td>
        </tr>
      </#list>
      </table>
      </p>
    </#if>
<#else>
    <h2 id="apiDataCategoryHeading"></h2>
    <p id="apiDataCategoryContent"></p>
</#if>
<#if browser.profileItemActions.allowList>

    <#if activeUser.apiVersion.versionOne>
        <h2>Profile Items</h2>
        <p>
            <table>
                <tr>
                    <th>Item</th>
                    <th>kgCO2 pcm</th>
                    <th>Name</th>
                    <th>Valid From</th>
                    <th>End</th>
                    <th>Actions</th>
                </tr>
                <#if sheet?? && 0 != sheet.rows?size>
                    <#list sheet.rows as row>
                    <tr id='Elem_${row.uid}'>
                        <td>${row.findCell('dataItemLabel')}</td>
                        <td>${row.findCell('amountPerMonth')}</td>
                        <td>${row.findCell('name')}</td>
                        <td>${row.findCell('validFrom')}</td>
                        <td><#if row.findCell('end') == 'true'>Yes<#else>No</#if></td>
                        <td>
                            <#if browser.profileItemActions.allowView><a href='${basePath}/${row.findCell('path')}'><img
                                src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
                            <#if browser.profileItemActions.allowDelete><input type="image"
                                                                               onClick="deleteProfileItem('${row.uid}', '${basePath}/${row.uid}'); return false;"
                                                                               src="/images/icons/page_delete.png" title="Delete"
                                                                               alt="Delete" border="0"/></#if>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </table>
        </p>
        <#if totalAmountPerMonth??>
            <p>Total kgCO2 Per Month: <span id="totalAmountPerMonth">${totalAmountPerMonth}</span></p>
        </#if>
    <#else>
        <h2 id="apiHeading"></h2>
        <div id="apiTopPager"></div>
        <table id="apiContent"></table>
        <p id="apiTAmount"></p>
        <div id="apiBottomPager"></div>
    </#if>

</#if>

<#if dataCategory.itemDefinition??>
  <#if browser.profileItemActions.allowCreate>
    <h2 id="createProfileHeading"></h2>
    <form id="createProfileFrm" onSubmit="return false;">
    <div id="createProfileItemDiv">
    </div>
    </form>
  </#if>
</#if>

<script type='text/javascript'>
    // api call
    <#if !activeUser.apiVersion.versionOne && browser.profileItemActions.allowList>

        document.observe('dom:loaded', function() {
            var profileCategoryApiService = new ProfileCategoryApiService(
                {
                    heading : "Profile Items",
                    headingElementName : "apiHeading",
                    contentElementName : "apiContent",
                    tAmountElementName : 'apiTAmount',
                    pagerTopElementName : 'apiTopPager',
                    pagerBtmElementName : 'apiBottomPager',
                    headingCategory : 'Profile Categories',
                    dataHeadingCategory : 'Profile Category Details',
                    dataHeadingCategoryElementName : 'apiDataCategoryHeading',
                    dataContentElementName : "apiDataCategoryContent",
                    apiVersion : '2.0',
                    drillDown : true,
                    allowView : ${browser.profileItemActions.allowView?string},
                    allowModify : ${browser.profileItemActions.allowModify?string},
                    allowDelete : ${browser.profileItemActions.allowDelete?string}
                });
                profileCategoryApiService.apiRequest();
        });
    </#if>
</script>


<#include '/includes/after_content.ftl'>