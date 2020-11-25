var baseURL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", (event) => {
    getServices();
});

document.querySelector("form").addEventListener("submit", (e) => {
    e.preventDefault();
    addService(new FormData(document.querySelector("form")));
});

function getServices() {
    var servicesTable = document.querySelector("table tbody");

    fetch(baseURL + "/v1/services")
      .then(response => response.json())
      .then(services => {
        if (services && services.length > 0) {
            services.forEach(service => servicesTable.append(createTableRow(service)));
        } else {
            servicesTable.append(createFullRow("There are no services to show."));
        }
      })
      .catch(error => servicesTable.append(
        createFullRow("Sorry, there has been an error and we are unable to get a list of services.")
      ));
}

function addService(formData) {
    // Ideally we could pass the formData object all the way to the backend, but that proved tricky

    var requestOptions = {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            "name": formData.get("serviceName"),
            "url": formData.get("serviceURL")
        })
    };

    fetch(baseURL + "/v1/services", requestOptions)
        // TODO: Check response code and throw error
        .then(response => response.text())
        .then(data => {
            document.querySelector("form").reset();
            window.location.reload();
        })
        .catch(error => console.log("Error when trying to add service: " + error));
}

function deleteService(serviceID) {
    fetch(baseURL + "/v1/services/" + serviceID, { method: "DELETE" })
        // TODO: Check response code and throw error
        .then(response => response)
        .then(data => window.location.reload())
        .catch(error => console.log("Error when trying to delete service: " + error));
}

function createFullRow(message) {
    var tr = document.createElement("tr");
    var td = document.createElement("td");

    td.innerHTML = message;
    td.colSpan = 8;
    tr.appendChild(td);

    return tr;
}

function createTableRow(service) {
    var tr = document.createElement("tr");

    tr.appendChild(createStatusTableCell(service.status));
    tr.appendChild(createTextTableCell(service.name));
    tr.appendChild(createAnchorTableCell(service.url));
    tr.appendChild(createTextTableCell(formatDateTime(service.created)));
    tr.appendChild(createTextTableCell(formatDateTime(service.lastUpdated)));
    tr.appendChild(createActionsContent(service));

    return tr;
}

function createTextTableCell(content) {
    var element = document.createElement("td");
    element.innerHTML = content;
    return element;
}

function createAnchorTableCell(url) {
    var anchor = document.createElement("a");
    anchor.href = url;
    anchor.innerHTML = url;

    var td = document.createElement("td");
    td.appendChild(anchor);

    return td;
}

function formatDateTime(isoString) {
    return new Date(Date.parse(isoString)).toLocaleString()
}

function createStatusTableCell(status) {
    var td = document.createElement("td");
    var div = document.createElement("div");

    if (status === "OK") {
        div.className = "alert alert-success";
    } else if (status === "FAIL") {
        div.className = "alert alert-danger";
    } else {
        div.className = "alert alert-secondary";
    }

    div.innerHTML = status;

    td.appendChild(div);
    return td;
}

function createActionsContent(service) {
    var td = document.createElement("td");
    td.appendChild(createDeleteButton(service));

    return td;
}

function createDeleteButton(service) {
    var button = document.createElement("button");

    button.type = "button";
    button.className = "btn btn-danger btn-sm";
    button.innerHTML = "Delete";

    button.addEventListener("click", (e) => {
        deleteService(service.id);
    });

    return button;
}
