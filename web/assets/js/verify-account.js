async function verifyAccount() {
    const code = document.getElementById("verificationCode").value;
    const messageEl = document.getElementById("message");

    try {
        const response = await fetch("VerifyAccount", {
            method: "POST",
            body: JSON.stringify({ code }),
            headers: { "Content-Type": "application/json" }
        });

        if (response.ok) {
            const json = await response.json();
            if (json.status) {
                messageEl.className = "text-success";
                messageEl.textContent = json.message;

                // Redirect after successful verification
                setTimeout(() => {
                    window.location.href = "index.html";
                }, 1500);  // delay so user can see success message
            } else {
                messageEl.className = "text-danger";
                messageEl.textContent = json.message;
            }
        } else {
            messageEl.className = "text-danger";
            messageEl.textContent = "Verification failed, please try again.";
        }
    } catch (err) {
        messageEl.className = "text-danger";
        messageEl.textContent = "Error occurred: " + err.message;
    }
}
