// Scroll-reveal: elements with .ts-reveal fade/rise in the first time they enter
// the viewport. A MutationObserver picks up elements added later (async data,
// route changes), so pages don't need any per-component wiring.
export function initReveal() {
  if (window.__tsRevealInit) return;
  window.__tsRevealInit = true;

  const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const io = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible');
          io.unobserve(entry.target);
        }
      });
    },
    { threshold: 0.12, rootMargin: '0px 0px -40px 0px' }
  );

  const attach = (root) => {
    if (!root.querySelectorAll) return;
    root.querySelectorAll('.ts-reveal:not(.is-visible)').forEach((el) => {
      if (reduced) {
        el.classList.add('is-visible'); // no motion: just show it
      } else {
        io.observe(el);
      }
    });
  };

  attach(document);

  const mo = new MutationObserver((mutations) => {
    mutations.forEach((m) => {
      m.addedNodes.forEach((node) => {
        if (node.nodeType !== 1) return;
        if (node.classList?.contains('ts-reveal')) attach(node.parentNode || document);
        attach(node);
      });
    });
  });
  mo.observe(document.body, { childList: true, subtree: true });

  // Navbar: add a soft shadow once the page is scrolled.
  const onScroll = () => {
    document
      .querySelector('.ts-nav')
      ?.classList.toggle('ts-nav--scrolled', window.scrollY > 8);
  };
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();
}
