/*
 * Copyright 2024 The Android Open Source Project
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

package androidx.privacysandbox.ui.integration.testapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.privacysandbox.ui.client.SandboxedUiAdapterFactory
import androidx.privacysandbox.ui.client.view.SandboxedSdkView
import androidx.privacysandbox.ui.integration.sdkproviderutils.SdkApiConstants.Companion.AdType
import androidx.privacysandbox.ui.integration.sdkproviderutils.SdkApiConstants.Companion.MediationOption
import androidx.privacysandbox.ui.integration.testaidl.ISdkApi
import com.google.android.material.switchmaterial.SwitchMaterial

class MainFragment : BaseFragment() {

    private lateinit var webViewBannerView: SandboxedSdkView
    private lateinit var bottomBannerView: SandboxedSdkView
    private lateinit var resizableBannerView: SandboxedSdkView
    private lateinit var newAdButton: Button
    private lateinit var resizeButton: Button
    private lateinit var resizeSdkButton: Button
    private lateinit var localWebViewToggle: SwitchMaterial
    private lateinit var mediationDropDownMenu: Spinner
    private lateinit var inflatedView: View
    private lateinit var sdkApi: ISdkApi

    override fun handleDrawerStateChange(isDrawerOpen: Boolean) {
        webViewBannerView.orderProviderUiAboveClientUi(!isDrawerOpen)
        bottomBannerView.orderProviderUiAboveClientUi(!isDrawerOpen)
        resizableBannerView.orderProviderUiAboveClientUi(!isDrawerOpen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inflatedView = inflater.inflate(R.layout.fragment_main, container, false)
        sdkApi = getSdkApi()
        onLoadedSdk()
        return inflatedView
    }

    private fun onLoadedSdk() {
        webViewBannerView = inflatedView.findViewById(R.id.webview_ad_view)
        bottomBannerView = SandboxedSdkView(requireActivity())
        resizableBannerView = inflatedView.findViewById(R.id.resizable_ad_view)
        newAdButton = inflatedView.findViewById(R.id.new_ad_button)
        resizeButton = inflatedView.findViewById(R.id.resize_button)
        resizeSdkButton = inflatedView.findViewById(R.id.resize_sdk_button)
        localWebViewToggle = inflatedView.findViewById(R.id.local_to_internet_switch)
        mediationDropDownMenu = inflatedView.findViewById(R.id.mediation_dropdown_menu)

        // Supply the mediation_option array to the mediationDropDownMenu spinner.
        ArrayAdapter.createFromResource(
                this.requireContext(),
                R.array.mediation_dropdown_menu_array,
                android.R.layout.simple_spinner_item
            )
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                mediationDropDownMenu.adapter = adapter
            }

        loadWebViewBannerAd()
        loadBottomBannerAd()
        loadResizableBannerAd()
    }

    private fun loadWebViewBannerAd() {
        webViewBannerView.addStateChangedListener()
        webViewBannerView.setAdapter(
            SandboxedUiAdapterFactory.createFromCoreLibInfo(
                sdkApi.loadBannerAd(
                    AdType.WEBVIEW,
                    MediationOption.NON_MEDIATED,
                    /* waitInsideOnDraw= */ false
                )
            )
        )

        localWebViewToggle.setOnCheckedChangeListener { _: View, isChecked: Boolean ->
            if (isChecked) {
                webViewBannerView.setAdapter(
                    SandboxedUiAdapterFactory.createFromCoreLibInfo(
                        sdkApi.loadBannerAd(
                            AdType.WEBVIEW_FROM_LOCAL_ASSETS,
                            MediationOption.NON_MEDIATED,
                            /*waitInsideOnDraw=*/ false
                        )
                    )
                )
            } else {
                webViewBannerView.setAdapter(
                    SandboxedUiAdapterFactory.createFromCoreLibInfo(
                        sdkApi.loadBannerAd(
                            AdType.WEBVIEW,
                            MediationOption.NON_MEDIATED,
                            /*waitInsideOnDraw=*/ false
                        )
                    )
                )
            }
        }
    }

    private fun loadBottomBannerAd() {
        bottomBannerView.addStateChangedListener()
        bottomBannerView.layoutParams =
            inflatedView.findViewById<LinearLayout>(R.id.bottom_banner_container).layoutParams
        requireActivity().runOnUiThread {
            inflatedView
                .findViewById<LinearLayout>(R.id.bottom_banner_container)
                .addView(bottomBannerView)
        }
        bottomBannerView.setAdapter(
            SandboxedUiAdapterFactory.createFromCoreLibInfo(
                sdkApi.loadBannerAd(
                    AdType.NON_WEBVIEW,
                    MediationOption.NON_MEDIATED,
                    /* waitInsideOnDraw= */ false
                )
            )
        )
    }

    private fun loadResizableBannerAd() {
        resizableBannerView.addStateChangedListener()
        resizableBannerView.setAdapter(
            SandboxedUiAdapterFactory.createFromCoreLibInfo(
                sdkApi.loadBannerAd(
                    AdType.NON_WEBVIEW,
                    MediationOption.NON_MEDIATED,
                    /* waitInsideOnDraw= */ true
                )
            )
        )

        newAdButton.setOnClickListener {
            // Mediation is enabled if Runtime-Runtime Mediation option or Runtime-App Mediation
            // option is selected.
            val selectedMediationOptionId = mediationDropDownMenu.selectedItemId
            val mediationEnabled =
                selectedMediationOptionId == MediationOption.SDK_RUNTIME_MEDIATEE.toLong() ||
                    selectedMediationOptionId == MediationOption.IN_APP_MEDIATEE.toLong()
            val appOwnedMediationEnabled =
                selectedMediationOptionId == MediationOption.IN_APP_MEDIATEE.toLong()
            if (mediationEnabled) {
                resizableBannerView.setAdapter(
                    SandboxedUiAdapterFactory.createFromCoreLibInfo(
                        if (appOwnedMediationEnabled) {
                            sdkApi.loadBannerAd(
                                AdType.NON_WEBVIEW,
                                MediationOption.IN_APP_MEDIATEE,
                                /*waitInsideOnDraw=*/ true
                            )
                        } else {
                            sdkApi.loadBannerAd(
                                AdType.NON_WEBVIEW,
                                MediationOption.SDK_RUNTIME_MEDIATEE,
                                /*waitInsideOnDraw=*/ true
                            )
                        }
                    )
                )
            } else {
                resizableBannerView.setAdapter(
                    SandboxedUiAdapterFactory.createFromCoreLibInfo(
                        sdkApi.loadBannerAd(
                            AdType.NON_WEBVIEW,
                            MediationOption.NON_MEDIATED,
                            /*waitInsideOnDraw=*/ true
                        )
                    )
                )
            }
        }

        val maxWidthPixels = 1000
        val maxHeightPixels = 1000
        val newSize = { currentSize: Int, maxSize: Int ->
            (currentSize + (100..200).random()) % maxSize
        }

        resizeButton.setOnClickListener {
            val newWidth = newSize(resizableBannerView.width, maxWidthPixels)
            val newHeight = newSize(resizableBannerView.height, maxHeightPixels)
            resizableBannerView.layoutParams =
                resizableBannerView.layoutParams.apply {
                    width = newWidth
                    height = newHeight
                }
        }

        resizeSdkButton.setOnClickListener {
            val newWidth = newSize(resizableBannerView.width, maxWidthPixels)
            val newHeight = newSize(resizableBannerView.height, maxHeightPixels)
            sdkApi.requestResize(newWidth, newHeight)
        }
    }
}
