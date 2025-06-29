import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 一个美化后的、通用的内容弹窗 Composable。
 *
 * @param onDismissRequest 当用户请求关闭弹窗时调用（例如点击外部区域或关闭按钮）。
 * @param modifier 应用于弹窗根部 Surface 的 Modifier。
 * @param width 弹窗的宽度。如果为 null，则根据内容自适应。
 * @param height 弹窗的高度。如果为 null，则根据内容自适应。
 * @param title 弹窗的标题。如果为 null，则不显示标题区域。
 * @param text 如果你只需要显示一段简单的居中文本，请使用此参数。它将被包裹在一个带居中对齐的 Box 中。
 * @param content 如果你需要更复杂的自定义布局，请使用此 lambda。
 */
@Composable
fun ContentPop(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = 320.dp,
    height: Dp? = null,
    title: String? = null,
    text: String? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    val thenModifier = modifier.then(
        if (width != null && height != null)
            Modifier.size(width, height)
        else if (width != null)
            Modifier.width(width)
        else if (height != null)
            Modifier.height(height)
        else Modifier
    ).border(1.dp, Color.White, RoundedCornerShape(10.dp))
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colors.surface,
            modifier = thenModifier
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = if (title != null) 24.dp else 48.dp,
                            start = 24.dp,
                            end = 24.dp,
                            bottom = 24.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colors.onSurface
                        )
                    }

                    when {
                        text != null -> {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f) // 10. 内容文本使用稍低的透明度，与标题区分
                                )
                            }
                        }

                        content != null -> {
                            content()
                        }
                    }
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(48.dp)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭弹窗",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
