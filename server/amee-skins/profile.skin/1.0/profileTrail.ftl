<p><a href='/profiles'>profiles</a> / <a href='/profiles/${profile.displayPath}'>${profile.displayPath}</a><#list pathItem.pathItems as p><#if p.path != ''> / <a href='/profiles/${profile.displayPath}${p.fullPath}'>${p.path}</a></#if></#list></p>
