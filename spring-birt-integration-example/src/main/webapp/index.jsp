<%@ taglib prefix="c" uri="http://www.springframework.org/tags" %>
<html>
<head>
</head>
<body>
<h1>BIRT Report</h1>

<p>
    <c:url value="/reports?reportName=TopNPercent.rptdesign" var= "topNPercent"/>
    <c:url value="/reports?reportName=SampleSpring.rptdesign" var= "sampleSpring"/>

    <a href  ="${topNPercent}">The Top N Percent Report </a>
    <br/>
    <a href  ="${sampleSpring}">Simple Spring-backed report </a>
</p>
 </body>
</html>