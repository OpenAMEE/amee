<#include 'profileCommon.ftl'>
<#include '/includes/furniture.ftl'>
<#include '/includes/before_content.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/profile_service.js" type="text/javascript"></script>

<script type="text/javascript">

    function profileCategoryLoaded() {
        $("totalAmountPerMonth").innerHTML = this.resource.totalAmountPerMonth;
    }

    function profileItemDeleted() {
        Effect.Fade(this.resourceElem);
        var profileCategoryResource = new ProfileCategoryResource('${profile.uid}', '${pathItem.fullPath}');
        profileCategoryResource.loadedCallback = profileCategoryLoaded;
        profileCategoryResource.load();
    }

    function deleteProfileItem(profileItemUid, profileItemPath) {
        if (profileItemPath && profileItemPath.indexOf("?") > -1) {
            resourceUrl = profileItemPath + '&method=delete';
        } else {
            resourceUrl = profileItemPath + '?method=delete';
        }
        resourceElem = $('Elem_' + profileItemUid);
        resourceType = 'Profile Item';
        var deleteResource = new DeleteResource();
        deleteResource.deleteResourceCallback = profileItemDeleted;
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }

    document.observe('dom:loaded', function() {
        <#if dataCategory.itemDefinition?? && browser.profileItemActions.allowCreate>
            new DrillDown(
                "/data${pathItem.fullPath}",
                "1.0",
                "yyyyMMdd",
                true
            ).loadDrillDown();
        </#if>
    });

</script>

<h1>Profile Category</h1>

<#include 'profileTrail.ftl'>

<h2>Profile Category Details</h2>
<p>Name: ${dataCategory.name}<br/>
    <#if dataCategory.path != ''>Path: ${dataCategory.path}<br/></#if>
    Full Path: ${basePath}<br/>
    <#if dataCategory.itemDefinition??>Item Definition: ${dataCategory.itemDefinition.name}<br/></#if>
    Environment: ${dataCategory.environment.name}<br/>
    Data Category UID: ${dataCategory.uid}<br/>
</p>

<#assign children = pathItem.findChildrenByType('DC')>
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

<#if browser.profileItemActions.allowList>
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
</#if>

<#if dataCategory.itemDefinition?? && browser.profileItemActions.allowCreate>
    <h2 id="createProfileHeading"></h2>
    <form id="createProfileFrm" onSubmit="return false;">
        <div id="createProfileItemDiv">
        </div>
    </form>
</#if>

<#include '/includes/after_content.ftl'>