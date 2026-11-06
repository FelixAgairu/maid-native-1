# Settings Page UI/UX Reordering Proposal

## Current Structure Analysis

The current settings screen follows this organization:
1. Language
2. Endpoint configuration
3. Model selection
4. Theme
5. Chat customization
6. Advanced features

## Proposed New Structure

Based on user workflow and importance, I propose this reordering:

### 1. **Connection & API Settings** (Most important - users need this to work)
   - Endpoint configuration (Base URL, API key)
   - Allow HTTP endpoints
   - Trust user-installed certificates
   - Endpoint presets
   - Scan button

### 2. **Model & Generation Settings** (Core functionality)
   - Model selection
   - Refresh models button
   - Sampling options
   - Tools configuration
   - MCP servers
   - Thinking toggle

### 3. **Chat Experience Settings** (User interaction)
   - Automatically name new chats
   - Scroll long messages
   - Export media
   - User name and avatar
   - Assistant name and avatar

### 4. **Appearance & Personalization** (Visual preferences)
   - Language selection
   - Theme section
   - Accent color
   - Composer background

### 5. **Information & Status** (Read-only information)
   - Token usage display

## Rationale

1. **Connection first**: Users need to configure their API endpoint before anything else works
2. **Model second**: Core functionality for generating responses
3. **Chat experience**: How users interact with the chat interface
4. **Appearance**: Visual preferences that don't affect functionality
5. **Information**: Read-only data at the bottom

## Implementation Plan

The changes will be made in `SettingsScreen.kt` by reordering the composable calls in the main Column. This is a UI-only change that doesn't affect functionality.

Let me implement this reordering now.
