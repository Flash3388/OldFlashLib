Welcome to FLASHLib, an extensive Java library providing algorithms and functionalities for a large amount of fields in programming.

The library consists of 5 main parts, each with different dependencies and usages:

- FLASHUtil: A utilities library
    - No requirements
    - Easy logging features
    - Quick and simple file IO
    - Both simple and advanced mathemetical functionalities, 
        like: Complex numbers, Matrices, Vectors, Fourier Transform, Interpolation, Integrals, Derivatives and more!
    
- FLASHComm: A communications management library
    - Requires FLASHUtil
    - Communication management
    - Can interface with multiple communication ports: IP based sockets, Serial, I2C and more
    - Camera data communication (Requires FLASHVision)
    
- FLASHVision: A vision library
    - Requires FLASHUtil, FLASHComm
    - Camera interfacing (Requires openCV)
    - Running of vision processing code from a remote controlling source or local
    - Sending and recieving of processing data
    - Dynamic vision processing system
    - Interfacing with any vision library
    - Built in openCV interfacing (Requires openCV)
    
- FLASHBot: A control and managment library for robots
    - Requires FLASHUtil, FLASHComm, FLASHVision
    - Robot control management: scheduling of actions and systems
    - Algorithms for advanced sensor-based movement
    - Algorithms for vision-based movement
    - Built in drive trains and generic systems
    - Interfacing with FLASHBoard
    - Compatible with the FRC RoboRIO platform (Requires WPILib)
    - Compatible with the Raspberry PI and BeagleBone Black platforms (Requires Bulldog)
    
- FLASHBoard: A custom driver's dashboard
   - Requires FLASHUtil, FLASHComm, FLASHVision
   - Easy-to-use remote control placement from connected robot controller
   - Camera display
   - Built in image processing (Requires openCV)
   - Built in SSH and SFTP clients (Requires SSJ)
   - Located in a different repository: https://gitlab.com/FLASH3388/FLASHboard

The library is still a work in progress and the repository is not user-friendly, as work progress this will change.
We plan to move the reporitory to GitHub, were it will be properly organized.

This is a product of FRC team FLASH 3388


