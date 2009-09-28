<#if !pageTitle??>
  <#assign pageTitle = "AMEE">
</#if>
<title>${pageTitle}</title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache" />
<meta http-equiv="Expires" content="-1" />
<meta name="Description" content=""/>
<meta name="Keywords" content="" />

<script src="/scripts/log/logger.js" type="text/javascript"></script>
<script src="/scripts/prototype/prototype.js" type="text/javascript"></script>
<script src="/scripts/scriptaculous/scriptaculous.js" type="text/javascript"></script>
<script src="/scripts/control/livepipe.js" type="text/javascript"></script>
<script src="/scripts/control/window.js" type="text/javascript"></script>
<script src="/scripts/control/tabs.js" type="text/javascript"></script>
<script src="/scripts/amee/resources.js" type="text/javascript"></script>

<style type="text/css" media="screen">
    @import url( /css/screen.css );
</style>

<script type="text/javascript" charset="utf-8">
    var is_ssl = ("https:" == document.location.protocol);
    var asset_host = is_ssl ? "https://s3.amazonaws.com/getsatisfaction.com/" : "http://s3.amazonaws.com/getsatisfaction.com/";
    document.write(unescape("%3Cscript src='" + asset_host + "javascripts/feedback-v2.js' type='text/javascript'%3E%3C/script%3E"));
</script>

<script type="text/javascript" charset="utf-8">
    var feedback_widget_options = {};
    feedback_widget_options.display = "overlay";
    feedback_widget_options.company = "amee";
    feedback_widget_options.placement = "right";
    feedback_widget_options.color = "#222";
    feedback_widget_options.style = "idea";
    var feedback_widget = new GSFN.feedback_widget(feedback_widget_options);
</script>

<script type="text/javascript">
    var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
    document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>

<script type="text/javascript">
    try {
        var pageTracker = _gat._getTracker("UA-498234-8");
        pageTracker._trackPageview();
    } catch(err) {
    }
</script>

<#include '/includes/localHead.ftl'>