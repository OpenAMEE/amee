<#include 'profileCommon.ftl'>
<#include '/includes/before_content.ftl'>
<script src="/scripts/amee/profile_service.js" type="text/javascript"></script>
<script type="text/javascript">
function profileCategoryLoaded() {
  <#if activeUser.apiVersion.versionOne>
    $("totalAmountPerMonth").innerHTML = this.resource.totalAmountPerMonth;
  <#else>
    Log.debug('delete');
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
  resourceUrl = profileItemPath + '?method=delete';
  resourceElem = $('Elem_' + profileItemUid);
  resourceType = 'Profile Item';
  var deleteResource = new DeleteResource()
  deleteResource.deleteResourceCallback = profileItemDeleted;
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>
<h1>Profile Category</h1>
<#include 'profileTrail.ftl'>
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
        <h2 id="tHeading"></h2>
        <p>
            <table id="tContent">
            </table>
        </p>
        <p id="tAmount"></p>
    </#if>

</#if>

<#if dataCategory.itemDefinition??>
  <#if browser.profileItemActions.allowCreate>
    <h2>Create Profile Item</h2>
    <p>
    <form onSubmit="return false;">
    <div id="createProfileItemDiv">
    </div>
    </form>
    </p>
  </#if>
</#if>

<script type='text/javascript'>

    // update drill down
    var drillDown = new DrillDown('/data${browser.pathItem.fullPath}');
    drillDown.loadDrillDown('');

    // api call
    document.observe('dom:loaded', function() {
        <#if !activeUser.apiVersion.versionOne && browser.profileItemActions.allowList>
            showJSON(renderApiResponse, "");
        </#if>
    });

    // section heading
    var heading = "Profile Items";

    function renderApiResponse(response) {

        var json = response.responseJSON; 

        // set section heading
        $("tHeading").innerHTML = heading;

        // create table headings
       var tableElement = new Element('table', {id : 'tContent'}).insert(getHeadingElement(json));

       // create table details
       var detailRows = getDetailRows(json);
       for (i = 0 ; i < detailRows.length; i++) {
           tableElement.insert(detailRows[i]);
       }

       // replace table
       $("tContent").replace(tableElement)

       // create total
       var totalElement = new Element('p', {id : 'tAmount'}).insert("Total " + getUnit(json) + " " + json.totalAmount.value);
        $("tAmount").replace(totalElement);
    }

    function getHeadingElement(json) {
        return new Element('tr')
                .insert(getHeadingData('item'))
                .insert(getHeadingData(getUnit(json)))
                .insert(getHeadingData('Name'))
                .insert(getHeadingData('Valid From'))
                .insert(getHeadingData('Valid To'))
                .insert(getHeadingData('Actions'));
    }

    function getHeadingData(heading) {
        return new Element('th').insert(heading);
    }

    function getUnit(json) {
        if (json.totalAmount) {
            return json.totalAmount.unit;
        }
        return "Unknown Unit";
    }
    function getDetailRows(json) {
        var rows = [];
        if (json.profileItems) {
            for (i = 0; i < json.profileItems.length; i++) {
                var profileItem = json.profileItems[i];
                var detailRow = new Element('tr', {id : profileItem.uid})
                    .insert(new Element('td', {id: profileItem.dataItem.uid}).insert(profileItem.dataItem.Label))
                    .insert(new Element('td').insert(profileItem.amount.value))
                    .insert(new Element('td').insert(profileItem.name))
                    .insert(new Element('td').insert(profileItem.startDate))
                    .insert(new Element('td').insert(profileItem.endDate));

                // create actions
                detailRow.insert(getActionsTableData(${browser.profileItemActions.allowView?string}, ${browser.profileItemActions.allowDelete?string}, profileItem.uid));

                // update array
                rows[i] = detailRow;
            }
            return rows;
        }
        rows[0] = new Element("tr").insert(new Element("td"));
        return rows;
    }

    function getActionsTableData(allowView, allowDelete, uid) {
        var actions = new Element('td');
        if (allowView) {
            actions.insert(new Element('a', {href : window.location.href + "/" + uid})
                .insert(new Element('img', {src : '/images/icons/page_edit.png', title : 'Edit', alt : 'Edit', border : 0 })));
        }

        if (allowDelete) {
            var dUrl = "'profileItem.uid','" + window.location.href + '/' + uid + "'";
            actions.insert(new Element('input',
                {
                onClick : 'deleteProfileItem(' + dUrl + ') ; return false;',
                type : 'image',
                src : '/images/icons/page_delete.png',
                title : 'Delete', alt : 'Delete', border : 0}));
        }
        return actions;
    }
</script>


<#include '/includes/after_content.ftl'>