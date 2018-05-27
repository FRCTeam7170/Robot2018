import picamera
import numpy as np
from matplotlib import pyplot as plt
import time
import grip


with picamera.PiCamera(resolution=(640, 480), framerate=30) as cam:
    time.sleep(2)
    pipeline = grip.GripPipeline()
    frame = np.empty((480, 640, 3), dtype=np.uint8)
    cam.capture(frame, format="rgb")
    pipeline.process(frame)
    plt.imshow(pipeline.hsv_threshold_output)
    plt.show()
