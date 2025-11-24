window.addEventListener("load", () => {
    const frames = [
        document.getElementById("frame1"),
        document.getElementById("frame2"),
        document.getElementById("frame3"),
        document.getElementById("frame4"),
    ];
    const stats = document.getElementById("stats");
    const overlay = document.getElementById("overlay");
    const overlayImg = document.getElementById("overlay-img");
    if (frames.some(f => !f) || !stats || !overlay || !overlayImg) {
        return;
    }
    // Set image sources (files must be in web/ folder)
    frames[0].src = "frame1.png";
    frames[1].src = "frame2.png";
    frames[2].src = "frame3.png";
    frames[3].src = "frame4.png";
    const resolution = "1280x720";
    const fps = 15;
    stats.textContent = `Resolution: ${resolution}, FPS: ${fps}`;
    // Click small image -> open big overlay
    frames.forEach(frame => {
        frame.addEventListener("click", () => {
            overlayImg.src = frame.src;
            overlay.style.display = "flex";
        });
    });
    // Click overlay to close
    overlay.addEventListener("click", () => {
        overlay.style.display = "none";
    });
});
