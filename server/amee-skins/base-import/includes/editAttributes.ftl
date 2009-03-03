<#if attributeLineItems??>
    <#list attributeLineItems as ali>
        <#if ali.widgetType == "TEXTAREA">
            <div class="columnBlock paddingTop clearfix">
                <div class="twoColSkinnyA alignRight"><label for="attr_${ali.key}">${ali.label?html}:</label></div>
                <div class="twoColFatB">
                    <textarea cols="40" rows="5" id="attr_${ali.key}" name="attr_${ali.key}" class="inputTextLong" style="<#if ali.attributeDefinition??>${ali.attributeDefinition.style}</#if>">${ali.value?html}</textarea>
                    <#if ali.attributeDefinition?? && ali.attributeDefinition.explanation != "">
                        <p class="tip">${ali.attributeDefinition.explanation?html}</p>
                    </#if>
                    <#if ali.inheritedValue != "">
                        <p class="tip">Leave blank to use the default value. The default value is '${ali.inheritedValue?html}'.</p>
                    </#if>
                </div>
            </div>
        <#elseif (ali.widgetType == "SELECT") && (ali.attributeDefinition??)>
            <div class="columnBlock paddingTop clearfix">
            <div class="twoColSkinnyA alignRight"><label for="attr_${ali.key}">${ali.label?html}:</label></div>
                <div class="twoColFatB">
                    <select id="attr_${ali.key}" name="attr_${ali.key}" class="inputSelectStd" style="${ali.attributeDefinition.style}">
                        <#list ali.attributeDefinition.optionChoices.choices as choice>
                            <option value="${choice.name}"<#if ali.resolvedValue == choice.name> selected="selected"</#if>>${choice.value}</option>
                        </#list>
                    </select>
                    <#if ali.attributeDefinition.explanation != "">
                        <p class="tip">${ali.attributeDefinition.explanation?html}</p>
                    </#if>
                </div>
            </div>
        <#elseif (ali.widgetType == "RADIO") && (ali.attributeDefinition??)>
            <fieldset class="noBorder">
            <legend class="noMargin noPadding">${ali.label?html}</legend>
            <#list ali.attributeDefinition.optionChoices.choices as choice>
                <div class="columnBlock clearfix">
                    <div class="twoColSkinnyA alignRight"><label for="attr_${ali.key}">${choice.value}:</label></div>
                    <div class="twoColFatB">
                        <input class="inputRadioStd" type="radio" id="attr_${ali.key}" name="attr_${ali.key}" value="${choice.name}"<#if ali.resolvedValue == choice.name> checked="checked"</#if> style="${ali.attributeDefinition.style}"/>
                    </div>
                </div>
            </#list>
            <#if ali.attributeDefinition.explanation != "">
                <p class="tip">${ali.attributeDefinition.explanation?html}</p>
            </#if>
            </fieldset>
        <#elseif (ali.widgetType == "CHECK") && (ali.attributeDefinition??)>
            <div class="columnBlock clearfix">
                <div class="twoColSkinnyA alignRight"><label for="attr_${ali.key}">${ali.label?html}:</label></div>
                <div class="twoColFatB">
                    <input class="inputCheckboxStd" type="checkbox" id="attr_${ali.key}" name="attr_${ali.key}" value="true"<#if ali.value == "true"> checked="checked"</#if> style="${ali.attributeDefinition.style}"/>
                    <#if ali.attributeDefinition.explanation != "">
                        <p class="tip">${ali.attributeDefinition.explanation?html}</p>
                    </#if>
                </div>
            </div>
        <#else>
            <div class="columnBlock paddingTop clearfix">
                <div class="twoColSkinnyA alignRight"><label for="attr_${ali.key}">${ali.label?html}:</label></div>
                <div class="twoColFatB">
                    <input id="attr_${ali.key}" name="attr_${ali.key}" type="text" value="${ali.value?html}" maxlength="100" size="50" class="inputTextLong" style="<#if ali.attributeDefinition??>${ali.attributeDefinition.style}</#if>"/>
                    <span class="errors"></span>
                    <#if ali.attributeDefinition?? && ali.attributeDefinition.explanation != "">
                        <p class="tip">${ali.attributeDefinition.explanation?html}</p>
                    </#if>
                    <#if ali.inheritedValue != "">
                        <p class="tip">Leave blank to use the default value. The default value is '${ali.inheritedValue?html}'.</p>
                    </#if>
                </div>
            </div>
        </#if>
    </#list>
</#if>