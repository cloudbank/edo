# edo
<img src="https://i.imgur.com/haEZ4TX.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
#
A demo backend focused app for eliminating duplicate images with Dagger2, Architecture including MVVM, Paging, LiveData, Room, Paging, and a custom dupe detector integrated with the Glide recyclerview preloader written in Kotlin.
It uses a modified dhash fingerprint without hamming distance to catch exact image duplicates.
Future releases may include a check for similar images within that epsilon.
The heart of the research is in using off heap and garbage free structures as well, tempting ART and Dalvik's GC and memory bounds and optimizing native invocation.

## with duplicate detection at preload
Preload image duplicate detector library under construction in experimental phase.
It currently uses SP to cache fingerprints and ids.  I am working on a garbage free solution and native 
optimizations.


## Edo v1   (POC demo portfolio app)
>>>>

<img src="https://i.imgur.com/wbstVq6.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
<img src="https://i.imgur.com/wbstVq6.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 

>>>>Target 27
>>>>SDK 14+, Oreo compatible, for phone and tablet

* Dagger2 for DI abstraction
* Roboelectric, Espresso, and Mockito tests
* Paging boundary callback for Room/Retrofit decision making
* MVVM, Repository, DAO patterns with LiveData for lifecycle managment
* Glide RecyclerView integration library for image preload
* Kotlin extensions
* Image duplicates removal built in at preload



>>>>VIEW   

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="https://i.imgur.com/wbstVq6.png" height="350"/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

### Designed with the Material theme and latest, greatest android libs for best viewing pleasure!
### 


### --> download from the [Google Play Store](https://play.google.com/store/apps/details?id=com.droidteahouse.edo)


## License

* [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## Building

This app was built with Android Studio which uses the gradle build system.  

## Acknowledgements

This project uses the [Github API] ( https://www.github.com)





