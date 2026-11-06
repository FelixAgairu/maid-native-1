# Implementation Plan for Requested Features

## ✅ Completed Features

1. **Add toggle that control custom certificates** ✅
   - Added `useCustomCertificates` setting
   - Added toggle above "Trust user-installed certificates"
   - Updated SettingsRepository, SettingsActions, ChatViewModel, MainActivity

2. **Add file load setting for custom CA certificates** ✅
   - Added "Load custom CA certificate" button below the toggles
   - Placeholder implementation ready for file picker integration

## 📋 Remaining Features to Implement

### High Priority (Core Functionality)

3. **Update API to get more info from model server**
   - Enhance OpenAiClient to extract more metadata
   - Update ContextUsage and TokenUsage models
   - Add context size reporting

4. **Change token info display to progress bar**
   - Update ComposerUsage.kt to show progress bar
   - Implement API context size reporting
   - Add color-based progress indicators

5. **Optimize Session Token Summary screen**
   - Update ChatPropertiesDialog.kt layout
   - Reorder and optimize info display

### Medium Priority (UI/UX Improvements)

6. **Change User/Agent avatar and nickname**
   - Reduce avatar size in MessageItem.kt
   - Extract main color from avatars
   - Apply color to message bubbles

7. **Add collapse mode for text bubbles**
   - Update MessageItem.kt collapse logic
   - Implement pair-wise collapsing
   - Keep latest message expanded

8. **Add bookmark/quick navigation sidebar**
   - Add DraggableScrollbar enhancements
   - Implement popup previews
   - Add bookmark functionality

### Low Priority (UI Tweaks)

9. **Change setting button to hamburger menu**
   - Update ChatMenu.kt icon
   - Add dropdown menu options

10. **Add batch actions to session screen**
    - Update ChatDrawer.kt for batch operations
    - Add selection mode
    - Implement batch delete/export

11. **Move Thinking/Reasoning to input area**
    - Update Composer.kt to add toggle
    - Move from SettingsScreen

12. **Move Auto Rename Chat to session screen**
    - Update ChatDrawer.kt
    - Move toggle from SettingsScreen

## 🚀 Implementation Strategy

### Phase 1: Core API and Data Features (Days 1-2)
- Feature 3: API enhancements
- Feature 4: Progress bar implementation
- Feature 5: Session summary optimization

### Phase 2: UI/UX Improvements (Days 3-4)
- Feature 6: Avatar and bubble styling
- Feature 7: Collapse mode
- Feature 8: Navigation sidebar

### Phase 3: UI Tweaks (Days 5)
- Feature 9: Hamburger menu
- Feature 10: Batch actions
- Feature 11: Move Thinking toggle
- Feature 12: Move Auto Rename toggle

## 📝 Implementation Notes

### Key Files to Modify

**Core API/Data:**
- `OpenAiClient.kt` - Enhance API calls
- `ContextUsage.kt` - Add context size tracking
- `TokenUsage.kt` - Update usage models
- `ComposerUsage.kt` - Progress bar implementation

**UI Components:**
- `MessageItem.kt` - Avatars, bubbles, collapse
- `ChatPropertiesDialog.kt` - Session summary
- `DraggableScrollbar.kt` - Navigation sidebar
- `Composer.kt` - Thinking toggle
- `ChatMenu.kt` - Hamburger menu
- `ChatDrawer.kt` - Batch actions, Auto Rename

**Data Layer:**
- `SettingsRepository.kt` - Already updated
- `ChatViewModel.kt` - Already updated
- `MainActivity.kt` - Already updated

### Testing Strategy

1. **Unit Tests**: Verify API enhancements
2. **UI Tests**: Test new components
3. **Integration Tests**: Ensure features work together
4. **User Testing**: Gather feedback on UX changes

### Risk Assessment

**High Risk:**
- API changes (may break compatibility)
- Progress bar implementation (complex UI)

**Medium Risk:**
- Collapse logic (may affect message display)
- Navigation sidebar (new UI element)

**Low Risk:**
- UI tweaks (visual only)
- Toggle moves (functional only)

## 📅 Timeline Estimate

- **Total Estimated Time**: 5-7 days
- **Daily Capacity**: 4-6 hours
- **Complexity**: High (multiple UI and API changes)

## 🎯 Next Steps

1. Start with API enhancements (Feature 3)
2. Implement progress bar (Feature 4)
3. Optimize session summary (Feature 5)
4. Proceed to UI improvements

Each feature should be implemented, tested, and verified before moving to the next.
