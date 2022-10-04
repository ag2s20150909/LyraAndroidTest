# Lyra V2 audio Android test samples

A Very Low-Bitrate Codec for Speech Compression

https://github.com/google/lyra/

## Set Sample Audio
Use FFmpeg to convert audio to wav format supported by lyra.
```agsl
ffmpeg -i test.flac -ac 1 -ar 48000 test.wav
```
Then place it in the `/storage/emulated/0/Android/data/me.ag2s.lyraandroidtest/cache/test.wav`

## Encode and  Decode

Click "Encode" and "Decode" to encode and decode.You can choose Bitrate on the right.

## Result File

All encoded and decoded files are in `/storage/emulated/0/Android/data/me.ag2s.lyraandroidtest/cache/`.

