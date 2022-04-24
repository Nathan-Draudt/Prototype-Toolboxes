# Example 1: short-term feature extraction
import sys
#from lib.pyAudioAnalysisMaster import pyAudioAnalysis
sys.path.insert(1,'lib/pyAudioAnalysisMaster/pyAudioAnalysis')
import ShortTermFeatures as aF
import audioBasicIO as aIO 
import numpy as np 
import plotly
import plotly.graph_objs as go 

import IPython


# read audio data from file 
# (returns sampling freq and signal as a numpy array)
fs, s = aIO.read_audio_file("lib/pyAudioAnalysisMaster/pyAudioAnalysis/data/3WORDS.wav")
##lib/pyAudioAnalysisMaster/pyAudioAnalysis/   possible extenssion needed
# play the initial and the generated files in notebook:
IPython.display.display(IPython.display.Audio("lib/pyAudioAnalysisMaster/pyAudioAnalysis/data/3WORDS.wav"))

# print duration in seconds:
duration = len(s) / float(fs)
print(f'duration = {duration} seconds')

# extract short-term features using a 50msec non-overlapping windows
win, step = 0.050, 0.050
[f, fn] = aF.feature_extraction(s, fs, int(fs * win), 
                                int(fs * step))
print(f'{f.shape[1]} frames, {f.shape[0]} short-term features')
print('Feature names:')
for i, nam in enumerate(fn):
    print(f'{i}:{nam}')
# plot short-term energy
# create time axis in seconds
time = np.arange(0, duration - step, win) 
# get the feature whose name is 'energy'
energy = f[fn.index('energy'), :]
mylayout = go.Layout(yaxis=dict(title="frame energy value"),
                     xaxis=dict(title="time (sec)"))
plotly.offline.iplot(go.Figure(data=[go.Scatter(x=time, 
                                                y=energy)], 
                               layout=mylayout))