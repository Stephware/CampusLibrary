// Keep native form navigation while showing progress and preventing repeat clicks.
document.addEventListener('submit', function (event) {
    const form = event.target;
    if (window.jQuery && window.jQuery.fn.valid && !window.jQuery(form).valid()) return;
    const button = event.submitter;
    if (!button || !button.dataset.submittingText) return;
    button.dataset.originalText = button.textContent;
    button.textContent = button.dataset.submittingText;
    button.disabled = true;
    form.setAttribute('aria-busy', 'true');
});
window.addEventListener('pageshow', function () {
    document.querySelectorAll('[data-original-text]').forEach(function (button) {
        button.textContent = button.dataset.originalText;
        button.disabled = false;
        button.form?.removeAttribute('aria-busy');
    });
});
