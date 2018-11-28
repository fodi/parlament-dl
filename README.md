# parlament-dl
This is a hastily coded utility for downloading videos from parlament.hu. Although it doesn't actually download anything, you only get a long list of URLs with the video chunks which than you can download however you like.

## Why?
Why not. But mostly for offline watching, or mirroring the downloaded videos. For example <a href="https://www.youtube.com/channel/UCFiYTlI_J7bWQKeLLeKE6wA">I upload them to YouTube</a>.

## How to use it?
Download the latest binary from the <a href="https://github.com/fodi/parlament-dl/releases">Releases</a> tab, extract the ZIP somewhere and run it like this:

`java -jar parlament-dl.jar [date] [resolution]`

The first argument is the date of the video you'd like to download in `yyyy-mm-dd` format, eg. `2018-09-17`. This argument is optional and defaults to today's date, but usually you want to specify this, since most videos are only available a day or two later than the actual date.

The second argument is the requested video resolution. Usually there are 4 different resolutions you can choose from: `448x252`, `640x360`, `896x504` and `1280x720`. This argument is also optional, it defaults to `1280x720`.

The application is fairly verbose and outputs log messages and the list of URLs to **stdout** and errors to **stderr**. So you probably want to save it's output to a file like this:

`java -jar parlament-dl.jar 2018-09-17 1280x720 >> 2018-09-17_1280x720.txt`

You can then import the list of URLs to whatever download manager. After you've downloaded the `.ts` files you probably want to concatenate them into a single video file. I use **ffmpeg** for this purpose.
