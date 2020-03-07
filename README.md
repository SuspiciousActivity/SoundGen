# SoundGen
A program to create sound from mathematical expressions.

# The idea
You give this program a file, in which there are one or more mathematical functions such as sin(440 * x * 2 * PI). SoundGen then reads all of your functions, interprets them, and produces a PCM 16 bit signed WAV file. It's easy to use, see the structure and examples:

# Structure
The first few lines consist of meta information, that is:
- The output file name
- The sample rate (such as 48000)
- The length of the sound (for example 6s)

After that, you can basically start typing functions. In one line, first enter the start time, then the end time, then the function with 'x' as the variable. 'x' will be in seconds, meaning if you enter `0s 1s sin(x)` this will result in "plotting" `sin(x)` from 0 to 1 and mapping it to the sound from 0 to 1 seconds. Functions are always plotted starting from 0, meaning `1s 2s sin(x)` will plot `sin(x)` from 0 to 1, but map it to 1 to 2 seconds. Functions *should* only range from -1 to 1, if they don't they will overflow. You could create a saw wave with that.

# Predefined functions
There are more functions available than just `sin(x)`!
- sin(x)
- cos(x)
- tan(x)
- floor(x)
- ceil(x)
- abs(x)
- random()
- PI (not a function, but a handy constant)

# Examples
## 440Hz sine
Say you want to create a normal sine wave with 440Hz, an A4, with 6 seconds length, 48000Hz sample rate.
```
OUT sine.wav
FREQ 48000
LEN 6s
START
0s 6s sin(440 * x * 2 * PI)
END
```

You can either save this into a file and pass the file as the first argument to the program or just start the program and paste it into its console. And that's it!

## Random noise
You can also create random noise by using one of the predefined functions!
```
OUT noise.wav
FREQ 48000
LEN 6s
START
0s 1s (random() - 0.5) * 2
END
```

Notice how this time, I used 0s to 1s, but the total length is still 6s! SoundGen automatically repeats your sound until it reaches the specified time, here 6s.
