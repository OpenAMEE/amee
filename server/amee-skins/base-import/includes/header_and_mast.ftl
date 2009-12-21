<#if !sectionName??>
    <#assign sectionName = "home">
</#if>
<#if !logo??>
    <#assign logo = true>
</#if>
<#if !logoUrl??>
    <#assign logoUrl = "/">
</#if>
<#if !logoTitle??>
    <#assign logoTitle = "AMEE">
</#if>
<#if !logoImageUrl??>
    <#assign logoImageUrl = "/clients/demo/images/jmLogo.gif">
</#if>
<#if !logoImageWidth??>
    <#assign logoImageWidth = "150">
</#if>
<#if !logoImageHeight??>
    <#assign logoImageHeight = "40">
</#if>
<!-- start top -->
<div id="head" class="clearfix">
    <div id="langBox">
        <!-- place holder for other meta info e.g. language -->
    </div>
    <div id="auth"><#if activeUser?? && activeUser.username != 'guest'>Hi ${activeUser.username} - <a href="/auth/signOut">Sign Out</a><#else><a href="/auth/signIn">Sign In</a></#if></div>
</div>
<div id="mastBox" class="clearfix">
    <div id="logo"><#if logo><a href="${logoUrl}" title="${logoTitle}"><img src="${logoImageUrl}" width="${logoImageWidth}" height="${logoImageHeight}" alt="${logoTitle}"/></a></#if></div>
    <div id="mastHead">
        &nbsp;
    </div>
    <div id="topNav" class="clearfix">
        <ul>
          <#include '/includes/navigation.ftl'>
        </ul>
    </div>
</div>
<!-- end top -->