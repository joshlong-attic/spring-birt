<%@ taglib prefix="c" uri="http://www.springframework.org/tags" %>
<html>
<head>
</head>
<body>
<h1>BIRT Report</h1>

<p>
    <c:url value="/reports?reportName=TopNPercent.rptdesign" var= "topNPercent"/>
    <c:url value="/reports?reportName=SampleSpring.rptdesign" var= "sampleSpring"/>
    <c:url value="/reports?reportName=masterreport.rptdesign" var= "master"/>
    <c:url value="/reports?reportName=DashBoard.rptdesign" var= "dashboard"/>

    <a href  ="${topNPercent}">The Top N Percent Report </a>
    <br/>
    <a href  ="${sampleSpring}">Simple Spring-backed report </a>
    <br/>
    <a href  ="${master}">Drill Through Example </a>    
    <br/>
    <a href  ="${dashboard}">Resource Folder Example</a>    
</p>
 </body>
</html>