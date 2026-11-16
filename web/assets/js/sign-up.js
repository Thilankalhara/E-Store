async function signUp() {
    const firstName = document.getElementById("firstName").value;
    const lastName = document.getElementById("lastName").value;
    const email = document.getElementById("email").value;
    const mobile = document.getElementById("mobile").value;
    const password = document.getElementById("password").value;

    const user = {
        firstName: firstName,
        lastName: lastName,
        email: email,
        mobile: mobile,
        password: password
    };
    
    const userJson = JSON.stringify(user);
    
    const response = await fetch("SignUp", {
        method: "POST",
        body: userJson,
        headers: {
            "Content-Type": "application/json"
        }
    });

    const messageEl = document.getElementById("message");

    if (response.ok) {
        const json = await response.json();
        if (json.status) {
            messageEl.className = "text-success";
            messageEl.innerHTML = json.message;
            window.location = "verify-account.html";
        } else {
            messageEl.className = "text-danger";
            messageEl.innerHTML = json.message;
        }
    } else {
        messageEl.className = "text-danger";
        messageEl.innerHTML = "Registration Failed. Please Try Again";
    }
}
