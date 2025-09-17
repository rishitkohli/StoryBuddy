
function showPage(pageId) {
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });
    document.getElementById(pageId).classList.add('active');
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    const activeLink = document.querySelector(`[data-page=${pageId}]`);
    if (activeLink) {
        activeLink.classList.add('active');
    }

    if (pageId === 'home') {
        // Clear all inputs
        ['generatorInput', 'analyzerInput', 'estimatorInput'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.value = '';
        });

        // Clear all outputs
        ['generatorResult', 'analyzerResult', 'estimatorResult'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.innerHTML = '';
        });

        // Clear file selected indicators
        ['generatorFileSelected', 'analyzerFileSelected', 'estimatorFileSelected'].forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.textContent = '';
                el.style.display = 'none';
            }
        });

        // Clear file inputs
        ['generatorFile', 'analyzerFile', 'estimatorFile'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.value = '';
        });

        // Clear selected role
        const selectedRole = document.getElementById('selectedRole');
        if (selectedRole) selectedRole.value = '';

        // Remove selected class from role cards
        document.querySelectorAll('.role-card.selected').forEach(card => {
            card.classList.remove('selected');
        });
    }

}

function handleFileUpload(pageType) {
    const fileInput = document.getElementById(pageType + 'File');
    const fileSelected = document.getElementById(pageType + 'FileSelected');
    const textarea = document.getElementById(pageType + 'Input');
    
    if (fileInput.files.length > 0) {
        const file = fileInput.files[0];
        fileSelected.textContent = `Selected: ${file.name}`;
        fileSelected.style.display = 'block';
        const reader = new FileReader();
        reader.onload = function(e) {
            textarea.value = e.target.result;
        };
        reader.readAsText(file);
    } else {
        fileSelected.textContent = '';
        fileSelected.style.display = 'none';
        textarea.value = '';
    }
}

function copyToClipboard(elementId) {
    const element = document.getElementById(elementId);
    const text = element.textContent;
    const tempTextArea = document.createElement('textarea');
    tempTextArea.value = text;
    document.body.appendChild(tempTextArea);
    tempTextArea.select();
    try {
        const successful = document.execCommand('copy');
        if (successful) {
            showToast('Copied to clipboard!');
        } else {
            throw new Error('Copy command failed');
        }
    } catch (err) {
        console.error('Failed to copy text: ', err);
        showToast('Failed to copy text', 'error');
    } finally {
        document.body.removeChild(tempTextArea);
    }
}

function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    toast.style.backgroundColor = type === 'error' ? '#dc2626' : '#059669';
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('show');
    }, 100);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            document.body.removeChild(toast);
        }, 300);
    }, 3000);
}

function showLoading(outputElementId, resultElementId) {
    const resultElement = document.getElementById(resultElementId);
    resultElement.innerHTML = '<div class="loading"><div class="spinner"></div>Generating...</div>';
}

async function generateStory() {
    const input = document.getElementById('generatorInput').value.trim();
    const resultElement = document.getElementById('generatorResult');
    if (!input) {
        showToast('Please enter requirements or upload a file.', 'error');
        return;
    }
    showLoading('generatorOutput', 'generatorResult');
    try {
        const res = await fetch("http://localhost:8080/api/chatbot-3/requirement-to-story", {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ requirement: input })
        });
        const data = await res.json();

        resultElement.innerHTML = data.message;
    } catch (error) {
        resultElement.textContent = "Error: " + error.message;
    }
}

function selectRole(selectedCardElement) {
    document.querySelectorAll('.role-card').forEach(card => {
        card.classList.remove('selected');
    });
    selectedCardElement.classList.add('selected');
    document.getElementById('selectedRole').value = selectedCardElement.dataset.role;
}

function generateSummary() {
    const input = document.getElementById('analyzerInput').value.trim();
    const role = document.getElementById('selectedRole').value;
    const resultElement = document.getElementById('analyzerResult');

    if (!input) {
        showToast('Please enter a JIRA story or upload a file.', 'error');
        return;
    }
    if (!role) {
        showToast('Please select a role using the cards.', 'error');
        return;
    }

    showLoading('analyzerOutput', 'analyzerResult');

    fetch("http://localhost:8080/api/chatbot-3", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ story: input, domain: role })
    })
    .then(res => res.json())
    .then(data => {
        const message = data.message;
        // Directly inject the HTML response into the result container
        resultElement.innerHTML = message;
    })
    .catch(error => {
        resultElement.innerHTML = `<p style='color:red;'>Error: ${error.message}</p>`;
    });
}


async function calculateEstimate() {
    const input = document.getElementById('estimatorInput').value.trim();
    const resultElement = document.getElementById('estimatorResult');
    if (!input) {
        showToast('Please enter a JIRA story or upload a file.', 'error');
        return;
    }
    showLoading('estimatorOutput', 'estimatorResult');
    try {
        const res = await fetch("http://localhost:8080/api/chatbot-3/estimate-effort", {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ story: input })
        });
        const data = await res.json();

        resultElement.innerHTML = data.message;
    } catch (error) {
        resultElement.textContent = "Error: " + error.message;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    showPage('home');
});


function setupSendToAnalyzerButton() {
    const sendButton = document.getElementById("send-to-analyzer");
    if (sendButton) {
        sendButton.addEventListener("click", function () {
            const generatorOutput = document.querySelector("#generatorResult");
            const analyzerInput = document.querySelector("#analyzerInput");
            if (generatorOutput && analyzerInput) {
                analyzerInput.value = generatorOutput.innerText.trim();
                const analyzerTab = document.querySelector('[data-page="analyzer"]');
                if (analyzerTab) analyzerTab.click();
            }
        });
    }
}

// Call this function when the DOM is fully loaded
document.addEventListener("DOMContentLoaded", setupSendToAnalyzerButton);


function setupSendToEstimatorButton() {
    const sendButton = document.getElementById("send-to-estimator");
    if (sendButton) {
        sendButton.addEventListener("click", function () {
            const generatorOutput = document.querySelector("#generatorResult");
            const estimatorInput = document.querySelector("#estimatorInput");
            if (generatorOutput && estimatorInput) {
                estimatorInput.value = generatorOutput.innerText.trim();
                const estimatorTab = document.querySelector('[data-page="estimator"]');
                if (estimatorTab) estimatorTab.click();
            }
        });
    }
}

// Call this function when the DOM is fully loaded
document.addEventListener("DOMContentLoaded", setupSendToEstimatorButton);

function setupSendToEstimator2Button() {
    const sendButton = document.getElementById("send-to-estimator2");
    if (sendButton) {
        sendButton.addEventListener("click", function () {
            const analyzerInput = document.querySelector("#analyzerInput"); // corrected ID
            const estimatorInput = document.querySelector("#estimatorInput");
            if (analyzerInput && estimatorInput) {
                estimatorInput.value = analyzerInput.value.trim(); // use .value instead of .innerText
                const estimatorTab = document.querySelector('[data-page="estimator"]');
                if (estimatorTab) estimatorTab.click();
            }
        });
    }
}


// Call this function when the DOM is fully loaded
document.addEventListener("DOMContentLoaded", setupSendToEstimator2Button);


function setupExportToExcelButton() {
    const exportButton = document.getElementById("export-to-excel");
    const roleCards = document.querySelectorAll(".role-card");

    roleCards.forEach(card => {
        card.addEventListener("click", () => {
            const role = card.dataset.role;
            exportButton.style.display = role === "QA" ? "inline-flex" : "none";
        });
    });

    exportButton.addEventListener("click", async () => {
        const toastId = showToast("Generating Excel...", true);
        const input = document.getElementById("analyzerInput").value.trim();
        if (!input) {
            showToast("Please enter a JIRA story or upload a file.", "error");
            return;
        }

        try {
            const response = await fetch("http://localhost:8080/api/chatbot-3/export-excel", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ story: input })
            });

            if (!response.ok) throw new Error("Failed to generate Excel");

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = "QA_Scenarios.xlsx";
            document.body.appendChild(a);
            a.click();
            a.remove();
        } catch (err) {
            showToast("Error exporting Excel: " + err.message, "error");
        }
    });
}

document.addEventListener("DOMContentLoaded", setupExportToExcelButton);


// Assuming showToast is already defined elsewhere in your script

document.querySelectorAll('.export-excel-btn').forEach(button => {
    button.addEventListener('click', async function () {
        // Show the generating toast
        const toastId = showToast('Generating Excel...', true); // true indicates persistent toast

        try {
            // Simulate the export process (replace with actual export logic)
            await exportToExcel(); // This should be your actual export function

            // Optionally update or hide the toast after success
            hideToast(toastId); // Hide the toast once done
        } catch (error) {
            // Handle error and update toast
            hideToast(toastId);
            showToast('Failed to generate Excel.', false);
            console.error('Export failed:', error);
        }
    });
});




    