<%@ taglib prefix="c" uri="http://www.springframework.org/tags" %>
<html>
<head>
</head>
<body>

<c:url value="/images/fez-diagram.jpg" var="fez"/>
<c:url value="/reports?reportName=TopNPercent.rptdesign" var="topNPercent"/>
<c:url value="/reports?reportName=SampleSpring.rptdesign" var="sampleSpring"/>
<c:url value="/reports?reportName=masterreport.rptdesign" var="master"/>
<c:url value="/reports?reportName=DashBoard.rptdesign" var="dashboard"/>

<img alt = "Some data is obvious..." src="${fez}"/>
<br/>

<h1>BIRT Report</h1>



<a href="${topNPercent}">The Top N Percent Report </a>
<br/>
<a href="${sampleSpring}">Simple Spring-backed report </a>
<br/>
<a href="${master}">Drill Through Example </a>
<br/>
<a href="${dashboard}">Resource Folder Example</a>

</body>
</html>