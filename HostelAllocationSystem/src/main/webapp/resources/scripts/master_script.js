function toggleMobileSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('mobileOverlay');

    console.log("🔄 Toggle clicked");

    if (sidebar && overlay) {
        console.log('   Current sidebar classes:', sidebar.classList.toString());

        if (sidebar.classList.contains('mobile-open')) {
            console.log("   Sidebar currently OPEN → closing now");
            closeMobileSidebar();
        } else {
            console.log("   Sidebar currently CLOSED → opening now");
            openMobileSidebar();
        }
    } else {
        console.error('❌ Toggle failed: Sidebar or overlay element not found');
    }
}

function openMobileSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('mobileOverlay');

    console.log("➡️ openMobileSidebar() called");

    if (sidebar && overlay) {
        sidebar.classList.add('mobile-open');
        overlay.classList.add('active');
        document.body.style.overflow = 'hidden';

        console.log('✅ Sidebar OPENED');
        console.log('   After open classes:', sidebar.classList.toString());
    } else {
        console.error('❌ Cannot open sidebar - elements not found');
    }
}

function closeMobileSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('mobileOverlay');

    console.log("➡️ closeMobileSidebar() called");

    if (sidebar && overlay) {
        sidebar.classList.remove('mobile-open');
        overlay.classList.remove('active');
        document.body.style.overflow = '';

        console.log('✅ Sidebar CLOSED');
        console.log('   After close classes:', sidebar.classList.toString());
    } else {
        console.error('❌ Cannot close sidebar - elements not found');
    }
}

// Initialize sidebar state on page load
document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('mobileOverlay');

    console.log('⚡ DOMContentLoaded → Window width:', window.innerWidth);

    if (sidebar && overlay && window.innerWidth <= 768) {
        sidebar.classList.remove('mobile-open');
        overlay.classList.remove('active');
        document.body.style.overflow = '';

        console.log('📱 Sidebar initialized CLOSED for mobile');
        console.log('   After init classes:', sidebar.classList.toString());
    } else {
        console.log('💻 Initialization skipped (desktop or missing elements)');
    }
});

console.log('✅ Master script loaded successfully');
