
<%@tag description="Frontier page template" pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<script defer="defer" src="//ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/js/script.js"></script>

<!-- The Foundation theme JavaScript -->
<script src="//ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/libraries/foundation-6/js/foundation.js"></script>
<script src="//ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/js/foundationExtendEBI.js"></script>
<script type="text/JavaScript">$(document).foundation();</script>
<script type="text/JavaScript">$(document).foundationExtendEBI();</script>

<script type="text/javascript">
    // document.addEventListener("DOMContentLoaded", function(event){
    //     ebiInjectAnnouncements({ headline: '', message: '<strong>Uploading large files to Annotare? ' +
    //             'We recommend using the Globus platform for a faster and more reliable transfer experience.  </strong>' +
    //             '<a href="https://www.ebi.ac.uk/fg/annotare/help/globus_upload.html" target="_blank">Learn more</a>', priority: 'warning' });

    document.addEventListener("DOMContentLoaded", function(event){
        ebiInjectAnnouncements({ headline: '', message: '<strong>Due to an EMBL-EBI-wide system outage, the Annotare submission service is disrupted.</strong>' +
                'You can create/edit experiments, but not submit them. File uploads work, but file import is paused. Processing delays expected.', priority: 'warning' });
        
        var notification_dive = document.getElementsByClassName("notifications-js")
        if (notification_dive.length > 0) {
            notification_dive[0].style.marginTop = "-1.5rem";
            notification_dive[0].style.marginBottom = "-1.5rem";
            notification_dive[0].style.position = "relative";
            notification_dive[0].style.width = "100vw";
            notification_dive[0].style.left = "calc(-49vw + 50%)"
        }
    })
    // var container = document.getElementById('main-content-area') || document.getElementById('main-content') || document.getElementById('main') || document.getElementById('content') || document.getElementById('contentsarea');
    // if (container == null) {
    //     var main = document.createElement('div');
    //     main.setAttribute("id", "main-content-area");
    //     document.body.insertBefore(main, document.body.firstChild);
    // }

</script>