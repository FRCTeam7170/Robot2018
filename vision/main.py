import picamera
import numpy as np
import grip
import time
from matplotlib import pyplot as plt


plt.ion()  # Non-blocking mode
plt.show()
with picamera.PiCamera(resolution=(640, 480), framerate=30) as cam:
    time.sleep(2)  # PiCamera setup time
    frame = np.empty((480, 640, 3), dtype=np.uint8)
    pipeline = grip.GripPipeline()
    for _ in cam.capture_continuous(frame, use_video_port=True, format="bgr"):
        pipeline.process(frame)
        plt.imshow(pipeline.hsv_threshold_output)
        plt.draw()
        plt.pause(1)  # Allow time to draw image
