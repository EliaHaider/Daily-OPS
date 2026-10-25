import sys, os, wave, struct, math
out = sys.argv[1]; os.makedirs(out, exist_ok=True); R = 22050
def tone(f, d, v=.7, decay=0):
    n = int(R * d); s = []
    for i in range(n):
        t = i / R
        e = math.exp(-decay * t) * min(1, i / (R * .004)) if decay else min(1, i / (R * .005), (n - i) / (R * .005))
        s.append(int(32767 * v * e * math.sin(2 * math.pi * f * t)))
    return s
def sil(d): return [0] * int(R * d)
def save(name, s):
    w = wave.open(os.path.join(out, name), 'wb'); w.setnchannels(1); w.setsampwidth(2); w.setframerate(R)
    w.writeframes(b''.join(struct.pack('<h', max(-32767, min(32767, x))) for x in s)); w.close()
a = []
for _ in range(14):
    for f in (880, 1175, 880, 1175): a += tone(f, .18) + sil(.07)
    a += sil(.6)
save('alarm_tone.wav', a)
save('water_tone.wav', tone(1320, .5, .6, 5) + tone(1760, .8, .6, 4))
save('sleep_tone.wav', tone(523, .9, .5, 2.5) + tone(659, 1.1, .5, 2.5))
w = []
for _ in range(2):
    for f in (660, 880, 1100): w += tone(f, .25) + sil(.08)
    w += sil(.4)
save('workout_tone.wav', w)
z=[]
for _ in range(5):
    for f in (392,494,587,784,587,494): z += tone(f,.5,.6,2.2)
save('azan_tone.wav', z)
save('task_tone.wav', tone(988,.25)+sil(.1)+tone(1319,.45,.6,4))
print('sounds OK')
