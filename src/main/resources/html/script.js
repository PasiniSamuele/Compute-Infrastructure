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

        while (form.firstChild) {
            form.firstChild.remove();
        }

        form.appendChild(getInputElement("directoryName", "Directory Name", "text"));
        form.appendChild(getInputElement("forceFailure", "Force Failure", "number"));
        form.appendChild(getTaskElement(task));

        let controlDiv = document.createElement("div");
        controlDiv.className = "control";

        let submitButton = document.createElement("button");
        submitButton.className = "button is-primary";
        submitButton.innerHTML = "Submit";

        controlDiv.appendChild(submitButton);
        form.appendChild(controlDiv);
    }

    function getInputElement(id, text, type) {
        let fieldDiv = document.createElement("div");
        fieldDiv.className = "field";

        let inputLabel = document.createElement("label");
        inputLabel.setAttribute("for", id);
        inputLabel.className = "label";
        inputLabel.innerHTML = text;

        fieldDiv.appendChild(inputLabel)

        let controlDiv = document.createElement("div");
        controlDiv.className = "control";

        let input = document.createElement("input");
        input.id = id;
        input.name = id;
        input.type = type;
        input.className = "input";
        input.placeholder = text;

        controlDiv.appendChild(input)
        fieldDiv.appendChild(controlDiv)

        return fieldDiv;
    }

    function getTaskElement(task) {
        switch (task) {
            case "compression":
                return getInputElement("compressionRatio", "Compression Ratio", "number");
            case "conversion":
                return getInputElement("targetFormat", "Target Format", "text");
            case "prime":
                return getInputElement("upperBound", "Upper Bound", "number");
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
                    document.getElementById("formDiv").classList.add("is-hidden");
                    document.getElementById("sseDiv").classList.remove("is-hidden");

                    let uuid = JSON.parse(req.responseText).uuid;
                    document.getElementById("taskUuid").innerText = uuid;

                    evtSource = new EventSource("/tasks/" + uuid);
                    evtSource.onmessage = onSseMessage;
                } else {
                    let errorMsg = document.getElementById("errorMessage");
                    errorMsg.innerText = "The server cannot accept tasks at the moment!"
                    errorMsg.classList.remove("is-hidden");
                }
            }
        };

        req.open("POST", form.action);
        req.setRequestHeader("Content-Type", "application/json");
        req.send(JSON.stringify(object));
    }

    function onSseMessage(event) {
        if (event.data !== "") {
            document.getElementById("waitingMsg").classList.add("is-hidden");
            document.getElementById("finishedMsg").classList.remove("is-hidden");

            evtSource.close();
        }
    }
})();