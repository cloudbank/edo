# edo
<img src="https://i.imgur.com/haEZ4TX.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
#
A demo backend focused app for eliminating duplicate images with Dagger2, Architecture including MVVM, Paging, LiveData, Room, Paging, and a custom dupe detector integrated with the Glide recyclerview preloader written in Kotlin.
It uses a modified dhash fingerprint algorithm to get matches within epsilon in C++ and coarse grained synnchronization with coroutines in Kotlin.

* [coroutines] I am able to match colorized images and some variations in resolution within a hamming distance of 3. I also reduced the search for similar images to within +/- 1 of the population count of the hash. I was able to refactor from a critical section and an ExecutorService to Kotlin coroutines and coarse grained synchronization, reducing  dhash times 99%! (3s to .006s)


<img src="https://i.imgur.com/ttPoUzB.jpg" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://i.imgur.com/zrC4nds.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://i.imgur.com/t8U16qn.jpg" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

(in seconds)

* hash147580 20 :: 1529688601:::time::0.009626643--15
* hash147462 18 :: 757939307:::time::0.005973385--17
* hash147344 17 :: 2914562479:::time::0.015411176--19
* hash147344 16 :: 2914562479:::time::0.007672204--19
* hash147066 15 :: 3329153925:::time::0.008064992--18
* hash147044 14 :: 1281709129:::time::0.008693842--13
* hash147798 25 :: 1697199892:::time::0.007136103--14
* hash147867 27 :: 1806095048:::time::0.044139966--16

#
<img src="https://i.imgur.com/TFhB1UV.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
<img src="https://i.imgur.com/WR6K0Sc.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
<img src="https://i.imgur.com/J6jHasJ.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 



## Edo v1.28   (POC demo portfolio app)
>>>>
Building a dhasher library within an image download API

>>>>Target 28
>>>>SDK 14+, Oreo compatible, for phone and tablet

* Dagger2 for DI abstraction
* Roboelectric, Espresso, and Mockito tests
* Paging boundary callback for Room/Retrofit decision making
* MVVM, Repository, DAO patterns with LiveData for lifecycle managment
* Glide RecyclerView integration library for image preload
* Kotlin extensions and coroutines
* Image duplicates removal built in at preload



### Designed with the Material theme and latest, greatest android libs for best viewing pleasure!
### 


### --> download from the [Google Play Store](https://play.google.com/store/apps/details?id=com.droidteahouse.edo)


## License

* [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## Building

This app was built with Android Studio which uses the gradle build system.  

## Acknowledgements

This project uses the [Github API] ( https://www.github.com)










