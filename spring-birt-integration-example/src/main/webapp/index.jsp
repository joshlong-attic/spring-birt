<%@ taglib prefix="c" uri="http://www.springframework.org/tags" %>
<html>
<head>
</head>
<body>

<c:url value="/images/fez-diagram.jpg" var="fez"/>
<c:url value="/TopNPercent" var="topNPercent"/>
<c:url value="/SampleSpring" var="sampleSpring"/>
<c:url value="/masterReport" var="master"/>
<c:url value="/DashBoard" var="dashboard"/>
<c:url value="/SubReports" var="subReports"/>

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
<br/>
<a href="${subReports}">Sub Reports Example</a>
<br/>

</body>
</html>