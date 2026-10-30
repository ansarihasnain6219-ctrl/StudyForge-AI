package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfilePrefs
import com.example.model.AppUiLanguage
import com.example.model.OutputLanguage

@Composable
fun SettingsScreen(
    prefs: UserProfilePrefs,
    onSetUiLanguage: (AppUiLanguage) -> Unit,
    onSetOutputLanguage: (OutputLanguage) -> Unit,
    onSaveProfile: (String) -> Unit,
    onSwitchToGuest: () -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onClearAllData: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isHi = prefs.uiLanguage == AppUiLanguage.HINDI
    var nameInput by remember(prefs.studentName) { mutableStateOf(prefs.studentName) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 780.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = if (isHi) "सेटिंग्स, प्रोफ़ाइल और गोपनीयता" else "Settings, Account & Privacy",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            // Guest Mode & Optional Student Account Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHi) "अतिथि मोड / वैकल्पिक छात्र प्रोफ़ाइल" else "Guest Mode & Optional Student Account",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (prefs.isGuestMode) {
                                if (isHi) "वर्तमान स्थिति: अतिथि मोड (Guest Mode) सक्रिय है। बिना साइन-अप के सभी सुविधाएं निःशुल्क उपलब्ध हैं।"
                                else "Current Status: Guest Mode Active. Full access with saved on-device history — no forced login or payment walls."
                            } else {
                                if (isHi) "सक्रिय छात्र प्रोफ़ाइल: ${prefs.studentName}"
                                else "Saved Student Profile: ${prefs.studentName}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = {
                                Text(if (isHi) "छात्र का नाम (वैकल्पिक)" else "Student Name (Optional Profile)")
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_name_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onSaveProfile(nameInput) },
                                modifier = Modifier.testTag("save_profile_button")
                            ) {
                                Text(if (isHi) "प्रोफ़ाइल सहेजें" else "Save Profile")
                            }
                            if (!prefs.isGuestMode) {
                                OutlinedButton(onClick = {
                                    nameInput = ""
                                    onSwitchToGuest()
                                }) {
                                    Text(if (isHi) "अतिथि मोड पर लौटें" else "Switch to Guest Mode")
                                }
                            }
                        }
                    }
                }
            }

            // Language & Theme Preferences
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isHi) "ऐप इंटरफ़ेस भाषा (App UI Language)" else "App Interface Language",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppUiLanguage.entries.forEach { lang ->
                                FilterChip(
                                    selected = prefs.uiLanguage == lang,
                                    onClick = { onSetUiLanguage(lang) },
                                    label = { Text(lang.label) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isHi) "डिफ़ॉल्ट प्रश्न जनरेशन भाषा" else "Default Question Output Language",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutputLanguage.entries.forEach { outLang ->
                                FilterChip(
                                    selected = prefs.outputLanguage == outLang,
                                    onClick = { onSetOutputLanguage(outLang) },
                                    label = { Text(if (isHi) outLang.labelHi else outLang.labelEn) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHi) "डार्क मोड (रात्रि अध्ययन थीम)" else "Dark Study Mode",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isHi) "डिफ़ॉल्ट रूप से क्लीन ब्लू और व्हाइट थीम सक्रिय रहती है"
                                    else "Clean Blue & White day theme is enabled by default",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = prefs.darkThemeEnabled,
                                onCheckedChange = onToggleDarkTheme,
                                modifier = Modifier.testTag("dark_theme_switch")
                            )
                        }
                    }
                }
            }

            // Data Deletion & Privacy Notice Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHi) "गोपनीयता सूचना और डेटा नियंत्रण" else "Privacy Notice & Data Controls",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHi) {
                                "• आपके अपलोड किए गए नोट्स केवल टेक्स्ट निष्कर्षण (OCR) और प्रश्न बनाने के लिए सुरक्षित HTTPS के माध्यम से Gemini API को भेजे जाते हैं।\n" +
                                "• आपके सहेजे गए नोट्स, जनरेटेड प्रश्न और टेस्ट इतिहास आपके डिवाइस पर स्थानीय रूप से संग्रहीत हैं और आप किसी भी समय इन्हें एक क्लिक में हटा सकते हैं।\n" +
                                "• StudyForge AI कभी भी अपने प्रश्नों को आधिकारिक बोर्ड परीक्षा लीक या गारंटीड पिछले वर्ष के प्रश्नपत्र (PYQ) होने का दावा नहीं करता है।\n" +
                                "• इस संस्करण में कोई भुगतान या सदस्यता शुल्क नहीं है।"
                            } else {
                                "• Uploaded PDFs and images are processed strictly over encrypted HTTPS with the Gemini API solely to extract text and generate questions from your study material.\n" +
                                "• Your extracted notes, generated papers, and test scores are stored locally on your device and can be deleted individually or completely at any time.\n" +
                                "• Academic Integrity: Generated questions are strictly grounded in your uploaded notes for self-practice and are never represented as official board PYQs or guaranteed exam papers.\n" +
                                "• Free Version 1.0: No payment features, hidden paywalls, or tracking ads."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onClearAllData,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delete_all_data_settings_button")
                        ) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isHi) "सभी अपलोड किए गए नोट्स और इतिहास हटाएं"
                                else "Delete All Uploaded Notes & Generated History"
                            )
                        }
                    }
                }
            }
        }
    }
}
