package com.ankit.familytools.data.model

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

object ToolCatalog {
    val allTools: List<ToolItem> = listOf(
        // PRIMARY SHORTCUTS / TOP PRIORITY
        ToolItem(
            id = "pdf_sign",
            title = "Sign PDF",
            hindiTitle = "दस्तावेज़ हस्ताक्षर",
            description = "Draw signature, place anywhere on page, resize and save.",
            hindiDescription = "हस्ताक्षर बनाएं, किसी भी पेज पर लगाएं और सेव करें।",
            category = ToolCategory.PDF,
            iconName = "Draw",
            route = "pdf_sign",
            isPrimaryShortcut = true
        ),
        ToolItem(
            id = "video_compress",
            title = "Compress Video",
            hindiTitle = "वीडियो कंप्रेस करें",
            description = "Reduce video file size for WhatsApp and sharing.",
            hindiDescription = "व्हाट्सएप और शेयरिंग के लिए वीडियो साइज़ छोटा करें।",
            category = ToolCategory.VIDEO,
            iconName = "Compress",
            route = "video_compress",
            isPrimaryShortcut = true
        ),
        ToolItem(
            id = "pdf_merge",
            title = "Merge PDF",
            hindiTitle = "पीडीएफ जोड़ें",
            description = "Combine multiple PDF documents into a single file.",
            hindiDescription = "कई पीडीएफ फाइलों को एक साथ जोड़कर एक फाइल बनाएं।",
            category = ToolCategory.PDF,
            iconName = "CallMerge",
            route = "pdf_merge",
            isPrimaryShortcut = true
        ),
        ToolItem(
            id = "image_crop",
            title = "Crop Image",
            hindiTitle = "फोटो क्रॉप करें",
            description = "Crop photos with preset ratios (1:1, 16:9) or freely.",
            hindiDescription = "फोटो को मनचाहे साइज़ या अनुपात में काटें।",
            category = ToolCategory.IMAGE,
            iconName = "Crop",
            route = "image_edit?action=crop",
            isPrimaryShortcut = true
        ),
        ToolItem(
            id = "image_to_pdf",
            title = "Images to PDF",
            hindiTitle = "फोटो से पीडीएफ",
            description = "Turn one or multiple photos into a clean PDF document.",
            hindiDescription = "एक या एक से अधिक फोटो को मिलाकर पीडीएफ बनाएं।",
            category = ToolCategory.PDF,
            iconName = "PictureAsPdf",
            route = "images_to_pdf",
            isPrimaryShortcut = true
        ),
        ToolItem(
            id = "doc_scan",
            title = "Scan & OCR",
            hindiTitle = "दस्तावेज़ स्कैन और ओसीआर",
            description = "Scan documents with camera and extract Hindi/English text.",
            hindiDescription = "कैमरे से स्कैन करें और फोटो से लिखा हुआ टेक्स्ट निकालें।",
            category = ToolCategory.PDF,
            iconName = "DocumentScanner",
            route = "doc_scan",
            isPrimaryShortcut = true
        ),

        // PDF TOOLS
        ToolItem(
            id = "pdf_split",
            title = "Split PDF",
            hindiTitle = "पीडीएफ अलग करें",
            description = "Split a large PDF by page ranges into smaller files.",
            hindiDescription = "बड़े पीडीएफ को पेजों के अनुसार अलग-अलग फाइलों में बांटें।",
            category = ToolCategory.PDF,
            iconName = "CallSplit",
            route = "pdf_split"
        ),
        ToolItem(
            id = "pdf_reorder",
            title = "Reorder Pages",
            hindiTitle = "पेज क्रम बदलें",
            description = "Visually reorder, rotate, or delete individual pages.",
            hindiDescription = "पेजों को आगे-पीछे करें, घुमाएं या अनचाहे पेज हटाएं।",
            category = ToolCategory.PDF,
            iconName = "RotateRight",
            route = "pdf_reorder"
        ),
        ToolItem(
            id = "pdf_compress",
            title = "Compress PDF",
            hindiTitle = "पीडीएफ साइज़ घटाएं",
            description = "Reduce PDF file size by optimizing embedded images.",
            hindiDescription = "पीडीएफ की इमेज को ऑप्टिमाइज़ करके फाइल साइज़ छोटा करें।",
            category = ToolCategory.PDF,
            iconName = "Compress",
            route = "pdf_compress"
        ),
        ToolItem(
            id = "pdf_text_to_pdf",
            title = "Text to PDF",
            hindiTitle = "टेक्स्ट से पीडीएफ",
            description = "Convert notes and text documents into readable PDF.",
            hindiDescription = "लिखित नोट्स या टेक्स्ट को साफ पीडीएफ में बदलें।",
            category = ToolCategory.PDF,
            iconName = "TextFields",
            route = "text_to_pdf"
        ),
        ToolItem(
            id = "pdf_extract_text",
            title = "Extract Text",
            hindiTitle = "टेक्स्ट निकालें",
            description = "Extract selectable text from PDF to copy or save.",
            hindiDescription = "पीडीएफ में से लिखा हुआ टेक्स्ट कॉपी या सेव करने के लिए निकालें।",
            category = ToolCategory.PDF,
            iconName = "TextFields",
            route = "pdf_extract_text"
        ),
        ToolItem(
            id = "pdf_watermark",
            title = "Add Watermark",
            hindiTitle = "वॉटरमार्क लगाएं",
            description = "Add custom confidential or family watermark text.",
            hindiDescription = "पीडीएफ के सभी पेजों पर अपना नाम या वॉटरमार्क लगाएं।",
            category = ToolCategory.PDF,
            iconName = "WaterDrop",
            route = "pdf_watermark"
        ),
        ToolItem(
            id = "pdf_page_numbers",
            title = "Page Numbers",
            hindiTitle = "पेज नंबर जोड़ें",
            description = "Add page numbers (Page X of Y) with custom position.",
            hindiDescription = "पीडीएफ में नीचे या ऊपर सुंदर पेज नंबर जोड़ें।",
            category = ToolCategory.PDF,
            iconName = "FormatListNumbered",
            route = "pdf_page_numbers"
        ),
        ToolItem(
            id = "pdf_protect",
            title = "Password Protect",
            hindiTitle = "पासवर्ड लॉक लगाएं",
            description = "Secure sensitive PDF documents with AES encryption.",
            hindiDescription = "जरूरी पीडीएफ फाइलों पर मजबूत पासवर्ड लॉक लगाएं।",
            category = ToolCategory.PDF,
            iconName = "Lock",
            route = "pdf_protect"
        ),
        ToolItem(
            id = "pdf_unlock",
            title = "Unlock PDF",
            hindiTitle = "पासवर्ड लॉक हटाएं",
            description = "Remove password from a PDF you know the password for.",
            hindiDescription = "पासवर्ड डालकर पीडीएफ का लॉक हमेशा के लिए हटाएं।",
            category = ToolCategory.PDF,
            iconName = "LockOpen",
            route = "pdf_unlock"
        ),
        ToolItem(
            id = "pdf_annotate",
            title = "Annotate PDF",
            hindiTitle = "पीडीएफ पर लिखें/हाइलाइट करें",
            description = "Draw freehand, highlight lines, and add text comments.",
            hindiDescription = "पेज पर हाथ से लिखें, जरूरी लाइन हाइलाइट करें या कमेंट लिखें।",
            category = ToolCategory.PDF,
            iconName = "Draw",
            route = "pdf_annotate"
        ),

        // IMAGE TOOLS
        ToolItem(
            id = "image_compress",
            title = "Compress Image",
            hindiTitle = "फोटो कंप्रेस करें",
            description = "Reduce photo KB/MB size while maintaining quality.",
            hindiDescription = "फोटो की क्वालिटी बनाए रखते हुए उसका केबी/एमबी साइज़ घटाएं।",
            category = ToolCategory.IMAGE,
            iconName = "Compress",
            route = "image_compress"
        ),
        ToolItem(
            id = "image_resize",
            title = "Resize Image",
            hindiTitle = "फोटो रिसाइज़ करें",
            description = "Change width and height in pixels or percentage.",
            hindiDescription = "फॉर्म भरने के लिए फोटो की चौड़ाई और ऊंचाई बदलें।",
            category = ToolCategory.IMAGE,
            iconName = "Transform",
            route = "image_edit?action=resize"
        ),
        ToolItem(
            id = "image_convert",
            title = "Convert Format",
            hindiTitle = "फोटो फॉर्मेट बदलें",
            description = "Convert between JPG, PNG, and WebP formats.",
            hindiDescription = "फोटो को जेपीजी, पीएनजी या वेबपी में बदलें।",
            category = ToolCategory.IMAGE,
            iconName = "Transform",
            route = "image_compress?action=convert"
        ),
        ToolItem(
            id = "image_collage",
            title = "Photo Collage",
            hindiTitle = "फोटो कोलाज बनाएं",
            description = "Combine multiple family photos into attractive grids.",
            hindiDescription = "2 से 6 फोटो मिलाकर सुंदर फैमिली कोलाज बनाएं।",
            category = ToolCategory.IMAGE,
            iconName = "PhotoLibrary",
            route = "image_collage"
        ),
        ToolItem(
            id = "image_adjust",
            title = "Adjust Colors",
            hindiTitle = "रंग और ब्राइटनेस सुधारें",
            description = "Fine-tune brightness, contrast, and color saturation.",
            hindiDescription = "फोटो की चमक, कंट्रास्ट और रंगों को साफ और सुंदर बनाएं।",
            category = ToolCategory.IMAGE,
            iconName = "Tune",
            route = "image_edit?action=adjust"
        ),
        ToolItem(
            id = "image_sign_stamp",
            title = "Sign on Photo",
            hindiTitle = "फोटो पर हस्ताक्षर लगाएं",
            description = "Place your saved signature onto any photo or form.",
            hindiDescription = "सरकारी फॉर्म या फोटो पर अपने हस्ताक्षर लगाएं।",
            category = ToolCategory.IMAGE,
            iconName = "Draw",
            route = "image_edit?action=sign"
        ),
        ToolItem(
            id = "image_remove_exif",
            title = "Remove Metadata (EXIF)",
            hindiTitle = "लोकेशन और डेटा हटाएं",
            description = "Strip GPS coordinates, camera model, and personal tags.",
            hindiDescription = "शेयर करने से पहले फोटो से लोकेशन और कैमरा डेटा हटाएं।",
            category = ToolCategory.IMAGE,
            iconName = "Lock",
            route = "image_edit?action=exif"
        ),

        // VIDEO TOOLS
        ToolItem(
            id = "video_trim",
            title = "Trim Video",
            hindiTitle = "वीडियो ट्रिम करें",
            description = "Cut out unwanted start and end parts from a video.",
            hindiDescription = "वीडियो के शुरुआत या अंत का फालतू हिस्सा काटें।",
            category = ToolCategory.VIDEO,
            iconName = "ContentCut",
            route = "video_trim"
        ),
        ToolItem(
            id = "video_mute",
            title = "Mute Video",
            hindiTitle = "वीडियो की आवाज़ बंद करें",
            description = "Remove background noise or sound from any video.",
            hindiDescription = "वीडियो में से बैकग्राउंड शोर या पूरी आवाज़ हटा दें।",
            category = ToolCategory.VIDEO,
            iconName = "VolumeOff",
            route = "video_trim?action=mute"
        ),
        ToolItem(
            id = "video_extract_audio",
            title = "Extract Audio",
            hindiTitle = "वीडियो से ऑडियो निकालें",
            description = "Save the audio track from a video as an M4A/AAC file.",
            hindiDescription = "वीडियो से केवल गाना या आवाज़ अलग करके ऑडियो फाइल बनाएं।",
            category = ToolCategory.VIDEO,
            iconName = "Audiotrack",
            route = "video_trim?action=extract_audio"
        ),
        ToolItem(
            id = "video_extract_frame",
            title = "Extract Photo Frame",
            hindiTitle = "वीडियो से फोटो निकालें",
            description = "Grab high-resolution still images from any video moment.",
            hindiDescription = "वीडियो के किसी भी पल को साफ फोटो के रूप में सेव करें।",
            category = ToolCategory.VIDEO,
            iconName = "AddPhotoAlternate",
            route = "video_trim?action=extract_frame"
        )
    )

    fun getToolById(id: String): ToolItem? = allTools.find { it.id == id }
}
