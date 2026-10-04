// The top bar's page menu (overrides/partials/header.html). Instant navigation keeps the header while the page changes,
// so the links are made absolute once, and the current page is marked again after every navigation.
(function () {
  var menu = document.querySelector("[data-pagemenu]");
  if (!menu) return;
  var links = Array.prototype.slice.call(menu.querySelectorAll("[data-path]"));
  var root = typeof __md_scope !== "undefined" ? __md_scope : new URL(".", location);

  links.forEach(function (link) {
    link.href = new URL(link.getAttribute("data-path"), root).href;
    link.addEventListener("click", function () { menu.open = false; });
  });

  function mark() {
    var here = location.pathname.replace(/index[.]html$/, "");
    links.forEach(function (link) {
      link.classList.toggle("pagemenu__link--active", new URL(link.href).pathname === here);
    });
    menu.open = false;
  }

  document.addEventListener("click", function (event) {
    if (menu.open && !menu.contains(event.target)) menu.open = false;
  });
  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") menu.open = false;
  });

  if (typeof document$ !== "undefined") document$.subscribe(mark);
  else mark();
})();
