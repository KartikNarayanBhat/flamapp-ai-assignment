window.addEventListener("load", () => {
  const frame = document.getElementById("frame") as HTMLImageElement | null;
  const stats = document.getElementById("stats") as HTMLDivElement | null;

  if (!frame || !stats) {
    return;
  }

  // TODO: copy a processed screenshot from your phone into web/frame.png
  frame.src = "frame.png";

  const resolution = "1280x720";
  const fps = 15;

  stats.textContent = `Resolution: ${resolution}, FPS: ${fps}`;
});
