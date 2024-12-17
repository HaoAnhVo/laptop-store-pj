document.addEventListener("DOMContentLoaded", function () {
    const alertMessage = document.getElementById("alert-message");
    if (alertMessage) {
        alertMessage.style.display = "flex";

        setTimeout(() => {
            alertMessage.style.display = "none";
        }, 3000);
    }
});