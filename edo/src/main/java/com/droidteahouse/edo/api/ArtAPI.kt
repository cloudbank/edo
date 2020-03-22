/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.droidteahouse.edo.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * API communication setup
 */


interface ArtAPI {
  @GET("/object?apikey=cd89c670-8570-11e8-afc1-95a6d2776c0b&period=248&worktype=print&size=10")
  fun japaneseDesign(
      @Query("page") page: String): Call<com.droidteahouse.edo.vo.EdoObjects>


}