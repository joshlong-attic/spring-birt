<%@ taglib prefix="c" uri="http://www.springframework.org/tags" %>
<html>
<head>
</head>
<body>
<h1>BIRT Report</h1>

<p>
    <c:url value="/reports?reportName=TopNPercent.rptdesign" var= "topNPercent"/>
    <c:url value="/reports?reportName=SampleSpring.rptdesign" var= "sampleSpring"/>

    ${topNPercent}
    ${sampleSpring}
<%--
    <%=response.encodeURL("/reports?reportName=TopNPercent.rptdesign")%>
    <%=response.encodeURL("/reports?reportName=SampleSpring.rptdesign")%>

    <a href="http://:8080/reports?reportName=TopNPercent.rptdesign">click here to run BIRT
        Report</a><br>
    <a href="http://localhost:8080/reports?reportName=SampleSpring.rptdesign">click here to run BIRT
        Report that calls a Spring Bean</a>--%>
</p>
<%= new java.util.Date() %>
</body>
</html>