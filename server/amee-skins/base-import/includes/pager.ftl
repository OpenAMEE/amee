<#if !pagerItemsLabel??>
    <#assign pagerItemsLabel = 'items'>
</#if>
<#if !pagerName??>
    <#assign pagerName = 'pagerTop'>
</#if>
<#if !pagerUrl??>
    <#assign pagerUrl = path>
</#if>
<#if !pagerLocalSort??>
    <#assign pagerLocalSort = "false">
</#if>
<#if !pagerJs??>
    <script type="text/javascript">
        function goToPage(url, page) {
            Form.disable('pagerTop');
            Form.disable('pagerBottom');
            if (url.indexOf('?') === -1) {
                url = url + '?';
            } else {
                url = url + '&';
            }
            url = url + 'page=' + page;
            window.location = url;
            return false;
        }
    </script>
    <#assign pagerJs = true>
</#if>

<form id="${pagerName}" name="${pagerName}" method="GET" action="${pagerUrl}" enctype="application/x-www-form-urlencoded">
    <#if pagerLocalSort != "false" && pagerName == "pagerTop">
        <div class="border textDiv padding marginTopBottom">
            <#include '/includes/localPagerSort.ftl'>
        </div>
    </#if>
    <div class="border textDiv padding">
        <button class="inputSubmitStd" type="submit" value="Previous page" onclick="return goToPage('${pagerUrl}', ${pager.previousPage?c});"<#if pager.atFirstPage> disabled="disabled"</#if>/>Previous page</button>
        &#160;
        <button class="inputSubmitStd" type="submit" value="Next page" onclick="return goToPage('${pagerUrl}', ${pager.nextPage?c});"<#if pager.atLastPage> disabled="disabled"</#if>/>Next page</button>
        &#160;
        Page&#160;
        <select class="inputSelectStd" size="1" name="page" onchange="goToPage('${pagerUrl}', this.value);">
            <#list 1..pager.lastPage as i>
                <option value="${i?c}"<#if i == pager.currentPage> selected="selected"</#if>>${i}</option>
            </#list>
        </select>&#160; of ${pager.lastPage}.
        &#160;
        <#if (pager.items > 0)>
            Showing ${pager.from} to ${pager.start + pager.itemsFound} of ${pager.items} ${pagerItemsLabel}.
        </#if>
    </div>
</form>