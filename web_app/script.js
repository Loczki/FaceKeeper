if (!("FaceDetector" in window)) {
  alert(
    "Your browser does not support the FaceDetector API. Please use Google Chrome."
  );
}

async function main() {
  try {
    // Select video and canvas elements
    const video = document.querySelector("#video");
    const canvas = document.querySelector("#canvas");
    const ctx = canvas.getContext("2d");

    // Get the webcam stream
    const stream = await navigator.mediaDevices.getUserMedia({ video: true });
    video.srcObject = stream;

    // Set up canvas size based on video
    video.addEventListener("loadeddata", () => {
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
    });

    // Create the face detection function
    async function detectFaces() {
      try {
        const faceDetector = new FaceDetector();
        const faces = await faceDetector.detect(video);

        // Clear the canvas and draw the video frame
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

        // Draw rectangles around detected faces
        ctx.strokeStyle = "red";
        ctx.lineWidth = 3;

        faces.forEach((face) => {
          const { width, height, top, left } = face.boundingBox;
          ctx.strokeRect(left, top, width, height);
        });
      } catch (err) {
        console.error("Error detecting faces:", err);
      }

      // Call the function continuously
      requestAnimationFrame(detectFaces);
    }

    // Start face detection
    detectFaces();
  } catch (err) {
    console.error("Error accessing webcam:", err);
    alert("Unable to access the webcam. Please check your permissions.");
  }
}

// Start the app
main();
