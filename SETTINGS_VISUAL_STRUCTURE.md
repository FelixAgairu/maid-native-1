# Settings Page Visual Structure

## 📱 Settings Screen Layout (After Reordering)

```
┌─────────────────────────────────────────────┐
│         SETTINGS PAGE                        │
│                                             │
├─────────────────────────────────────────────┤
│                                             │
│  ┌─────────────────────────────────────────┐  │
│  │ 1. CONNECTION & API SETTINGS            │  │
│  │                                         │  │
│  │  • Endpoint (Base URL, API key)         │  │
│  │  • Allow HTTP endpoints                 │  │
│  │  • Trust user certificates              │  │
│  │  • Endpoint presets                     │  │
│  │  • Scan button                          │  │
│  └─────────────────────────────────────────┘  │
│                                             │
│  ┌─────────────────────────────────────────┐  │
│  │ 2. MODEL & GENERATION SETTINGS         │  │
│  │                                         │  │
│  │  • Model selection                      │  │
│  │  • Refresh models                       │  │
│  │  • Sampling options                     │  │
│  │  • Tools configuration                  │  │
│  │  • MCP servers                          │  │
│  │  • Thinking toggle                      │  │
│  └─────────────────────────────────────────┘  │
│                                             │
│  ┌─────────────────────────────────────────┐  │
│  │ 3. CHAT EXPERIENCE SETTINGS            │  │
│  │                                         │  │
│  │  • Auto-name chats                     │  │
│  │  • Scroll long messages                 │  │
│  │  • User name & avatar                  │  │
│  │  • Assistant name & avatar             │  │
│  │  • Export media                         │  │
│  └─────────────────────────────────────────┘  │
│                                             │
│  ┌─────────────────────────────────────────┐  │
│  │ 4. APPEARANCE & PERSONALIZATION        │  │
│  │                                         │  │
│  │  • Language selection                   │  │
│  │  • Theme (Accent color)                 │  │
│  │  • Composer background                  │  │
│  └─────────────────────────────────────────┘  │
│                                             │
│  ┌─────────────────────────────────────────┐  │
│  │ 5. INFORMATION & STATUS                 │  │
│  │                                         │  │
│  │  • Token usage display                  │  │
│  └─────────────────────────────────────────┘  │
│                                             │
└─────────────────────────────────────────────┘
```

## 📊 Section Breakdown

### Section 1: Connection & API Settings (Lines 123-224)
**Purpose**: Configure how the app connects to AI services
**Priority**: High (required to use the app)
**Components**:
- Endpoint configuration (Base URL, API key)
- Security settings (HTTP allowance, certificates)
- Preset management
- Network scanning

### Section 2: Model & Generation Settings (Lines 226-308)
**Purpose**: Control AI model behavior and generation parameters
**Priority**: High (core functionality)
**Components**:
- Model selection
- Refresh models button
- Sampling parameters (temperature, Top P, etc.)
- Tool permissions
- MCP server configuration
- Reasoning/thinking toggle

### Section 3: Chat Experience Settings (Lines 310-332)
**Purpose**: Customize how users interact with the chat interface
**Priority**: Medium (affects daily usage)
**Components**:
- Chat naming preferences
- Message display options
- User and assistant profiles
- Media export settings

### Section 4: Appearance & Personalization (Lines 334-343)
**Purpose**: Visual customization and localization
**Priority**: Low (cosmetic preferences)
**Components**:
- Language selection
- Theme customization
- Accent colors
- Composer background images

### Section 5: Information & Status (Lines 345-354)
**Purpose**: Display system information and usage statistics
**Priority**: Low (read-only information)
**Components**:
- Token usage summary
- Storage information

## 📈 Scroll Flow

Users now experience a natural scrolling flow:

1. **Top**: Start with connection setup (most important)
2. **Middle**: Configure AI behavior (core functionality)
3. **Lower Middle**: Customize chat experience (daily usage)
4. **Bottom**: Visual preferences (less frequent changes)
5. **Very Bottom**: System information (read-only)

## 🎯 User Journey

### New User:
1. Scrolls to Section 1 → Configures endpoint
2. Scrolls to Section 2 → Selects model
3. Scrolls to Section 3 → Sets up profiles
4. Scrolls to Section 4 → Chooses language/theme
5. Done! App is ready to use

### Existing User:
1. Scrolls directly to needed section
2. Finds settings quickly due to logical grouping
3. Makes changes without confusion
4. Saves and continues

### Power User:
1. Navigates to advanced sections (2, 3)
2. Configures tools, MCP, sampling
3. Customizes all aspects
4. Optimizes performance

## 🔄 Comparison with Original

### Original Structure:
```
Language → Endpoint → Model → Theme → Chat → (scattered toggles)
```

### New Structure:
```
Connection → Model → Chat → Appearance → Information
```

### Key Differences:
- ✅ **Logical progression** instead of arbitrary order
- ✅ **Priority-based** instead of alphabetical
- ✅ **Grouped by function** instead of scattered
- ✅ **User-centric** instead of feature-centric

## 🎨 Visual Hierarchy

The new structure creates a clear visual hierarchy:

1. **Primary Sections** (1-2): Large, prominent, essential
2. **Secondary Sections** (3-4): Medium, important but optional
3. **Tertiary Section** (5): Small, informational only

## 📱 Responsive Design

The layout remains fully responsive:
- ✅ Works on all screen sizes
- ✅ Proper scrolling behavior
- ✅ Keyboard-friendly (IME padding)
- ✅ Touch-optimized (large tap targets)

## 🔍 Code Structure

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp),
) {
    // 1. CONNECTION & API SETTINGS
    // ... endpoint configuration ...
    
    // 2. MODEL & GENERATION SETTINGS
    // ... model selection ...
    
    // 3. CHAT EXPERIENCE SETTINGS
    // ... chat preferences ...
    
    // 4. APPEARANCE & PERSONALIZATION
    // ... theme settings ...
    
    // 5. INFORMATION & STATUS
    // ... usage display ...
}
```

## ✅ Benefits of This Structure

1. **Intuitive**: Follows natural user workflow
2. **Efficient**: Faster to find and change settings
3. **Logical**: Related settings grouped together
4. **Scalable**: Easy to add new sections
5. **Maintainable**: Clear code organization
6. **Accessible**: Proper spacing and hierarchy
7. **Responsive**: Works on all devices

## 🚀 Future Enhancements

Potential improvements based on this structure:

1. **Visual Dividers**: Add subtle lines between sections
2. **Section Headers**: Make section titles more prominent
3. **Collapsible Sections**: Allow hiding less-used sections
4. **Search**: Add search functionality for settings
5. **Quick Actions**: Add frequently-used settings to top
6. **Contextual Help**: Add tooltips for each section

---

*Visual structure created to aid understanding of the new settings organization*
