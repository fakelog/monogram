package org.monogram.presentation.features.stickers.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.monogram.domain.models.MessageModel
import org.monogram.domain.models.RecentEmojiModel
import org.monogram.presentation.core.util.AppPreferences
import org.monogram.presentation.di.appInject
import org.monogram.presentation.features.chats.currentChat.components.chats.getEmojiFontFamily
import org.monogram.presentation.features.stickers.ui.view.StickerImage


@Composable
fun ReactionsRow(
    message: MessageModel,
    availableReactions: List<String>,
    suppressAppearanceAnimation: Boolean,
    onAppearanceAnimationConsumed: () -> Unit,
    onReactionsChanged: (Int) -> Unit,
    onReaction: (String) -> Unit,
    appPreferences: AppPreferences = appInject()
) {
    val haptic = LocalHapticFeedback.current

    val context = LocalContext.current
    val emojiStyle by appPreferences.emojiStyle.collectAsState()
    val emojiFontFamily = remember(context, emojiStyle) { getEmojiFontFamily(context, emojiStyle) }

    val reactions = availableReactions.map { remember(it) { RecentEmojiModel(it) } }

    val chosenSet = remember(message.reactions) {
        message.reactions
            .filter { it.isChosen }
            .map { it.emoji }
            .toSet()
    }

    LaunchedEffect(reactions.size) {
        onReactionsChanged(reactions.size)
    }

    LaunchedEffect(suppressAppearanceAnimation, reactions.isNotEmpty()) {
        if (suppressAppearanceAnimation && reactions.isNotEmpty()) {
            onAppearanceAnimationConsumed()
        }
    }

    AnimatedVisibility(
        visible = reactions.isNotEmpty(),
        enter = if (suppressAppearanceAnimation) {
            EnterTransition.None
        } else {
            fadeIn(animationSpec = tween(150, easing = LinearOutSlowInEasing))
        },
        exit = fadeOut(animationSpec = tween(100, easing = FastOutLinearInEasing)),
        label = "ReactionsRowVisibility"
    ) {
        Column {
            LazyRow(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = reactions,
                    key = { it.emoji }
                ) { reaction ->
                        val isChosen = reaction.emoji in chosenSet

                        val backgroundColor by animateColorAsState(
                            targetValue = if (isChosen) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "reactionBg"
                        )

                        val scale by animateFloatAsState(
                            targetValue = if (isChosen) 1.06f else 1f,
                            animationSpec = tween(durationMillis = 160, easing = LinearOutSlowInEasing),
                            label = "reactionScale"
                        )

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clip(CircleShape)
                                .background(backgroundColor)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onReaction(reaction.emoji)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val sticker = reaction.sticker
                            if (sticker != null) {
                                StickerImage(
                                    path = sticker.path,
                                    modifier = Modifier.size(28.dp),
                                )
                            } else {
                                Text(
                                    text = reaction.emoji,
                                    fontSize = 24.sp,
                                    fontFamily = emojiFontFamily
                                )
                            }
                        }

                }
            }
        }
    }
}