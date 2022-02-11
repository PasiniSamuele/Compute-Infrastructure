(function () {
    let taskSelect, form, evtSource;

    window.addEventListener("load", () => {
        taskSelect = document.getElementById("taskSelect");
        form = document.getElementById("form")

        taskSelect.addEventListener("change", onTaskSelectChange);
        onTaskSelectChange();

        form.addEventListener("submit", onFormSubmit)
    });

    function onTaskSelectChange() {
        const task = taskSelect.value;
        form.action = "/tasks/" + task;
        form.innerHTML = "<label for='directoryName'>Directory Name:</label>" +
            "<input id='directoryName' name='directoryName' type='text'><br>" +
            getTaskInput(task) +
            "<label for='forceFailure'>Force Failure:</label>" +
            "<input id='forceFailure' name='forceFailure' type='number'><br><br>" +
            "<input type='submit' id='submitButton' value='Submit'>"
    }

    function getTaskInput(task) {
        switch (task) {
            case "compression":
                return "<label for='compressionRatio'>Compression Ratio:</label>" +
                    "<input id='compressionRatio' name='compressionRatio' type='number'><br>"
            case "conversion":
                return "<label for='targetFormat'>Target Format:</label>" +
                    "<input id='targetFormat' name='targetFormat' type='text'><br>"
            case "prime":
                return "<label for='upperBound'>Upper Bound:</label>" +
                    "<input id='upperBound' name='upperBound' type='number'><br>"
        }
    }

    function onFormSubmit(event) {
        event.preventDefault();
        let formData = new FormData(form);

        let object = {};
        formData.forEach(function (value, key) {
            object[key] = value;
        });

        let req = new XMLHttpRequest();
        req.onload = function () {
            if (req.readyState === req.DONE) {
                if (req.status === 202) {
                    document.getElementById("formDiv").style.display = "none";
                    document.getElementById("sseDiv").style.display = "block";

                    evtSource = new EventSource("/tasks/" + JSON.parse(req.responseText).uuid);
                    evtSource.onmessage = onSseMessage;
                } else {
                    let errorMsg = document.getElementById("errorMessage");
                    errorMsg.innerText = "The server cannot accept tasks at the moment!"
                    errorMsg.style.display = "block";
                }
            }
        };

        req.open("POST", form.action);
        req.setRequestHeader("Content-Type", "application/json");
        req.send(JSON.stringify(object));
    }

    function onSseMessage(event) {
        if (event.data !== "") {
            document.getElementById("waitingMsg").style.display = "none";
            document.getElementById("finishedMsg").style.display = "block";

            evtSource.close();
        }
    }
})();