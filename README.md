# edo
<img src="https://i.imgur.com/haEZ4TX.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
#
<img src="https://i.imgur.com/pAjy0QO.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
 <img src="https://i.imgur.com/7GVKIzx.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 



branches:

* [hammingWeight] uses a radical approach wrt GC by filtering images first with an int stored in a direct buffer that serves as a bitcount bitset. I am able to match colorized images and some variations in resolution within a hamming distance.  I am working on making it faster, more efficient, and refactoring it into a libray.  Amazingly, the NN is working without help from glide, but I need to download a fairly large image to get enough info for a fingerprint that works. 

* [master] soon will merge hammingWeight

* [kotlinDhash] with the java/kotlin side implementation of CSS--so far native is not worse

A demo backend focused app for eliminating duplicate images with Dagger2, Architecture including MVVM, Paging, LiveData, Room, Paging, and a custom dupe detector integrated with the Glide recyclerview preloader written in Kotlin.
It uses a modified dhash fingerprint algorithm to get matches within epsilon.

The heart of the research is in using off heap and garbage free structures as well, tempting ART and Dalvik's GC and memory bounds and optimizing native invocation.

## with duplicate detection at preload
Preload image duplicate detector library under construction in experimental phase.
It currently uses SharedPrefs to persist hashes and ids and an int bitset stored in a direct buffer as a filter.


## Edo v1   (POC demo portfolio app)
>>>>


>>>>Target 27
>>>>SDK 14+, Oreo compatible, for phone and tablet

* Dagger2 for DI abstraction
* Roboelectric, Espresso, and Mockito tests
* Paging boundary callback for Room/Retrofit decision making
* MVVM, Repository, DAO patterns with LiveData for lifecycle managment
* Glide RecyclerView integration library for image preload
* Kotlin extensions
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










