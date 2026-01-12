package com.app.sino.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.app.sino.ui.theme.Dimens
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoLightGrey
import com.app.sino.ui.theme.SinoWhite

@Composable
fun SinoTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    placeholder: String? = null,
    trailingIcon: ImageVector? = null,
    @DrawableRes trailingIconRes: Int? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = SinoWhite
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.TextFieldCornerRadius),
            placeholder = if (placeholder != null) {
                { Text(text = placeholder, color = Color.Gray, style = MaterialTheme.typography.bodyMedium) }
            } else null,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SinoWhite.copy(alpha = 0.1f),
                unfocusedContainerColor = SinoWhite.copy(alpha = 0.1f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = SinoWhite,
                focusedTextColor = SinoWhite,
                unfocusedTextColor = SinoWhite,
                errorContainerColor = SinoWhite.copy(alpha = 0.1f)
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            singleLine = true,
            isError = isError,
            trailingIcon = if (trailingIcon != null || trailingIconRes != null) {
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        if (trailingIconRes != null) {
                            Icon(
                                painter = painterResource(id = trailingIconRes),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (isError) MaterialTheme.colorScheme.error else SinoWhite.copy(alpha = 0.7f)
                            )
                        } else if (trailingIcon != null) {
                            Icon(
                                imageVector = trailingIcon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (isError) MaterialTheme.colorScheme.error else SinoWhite.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else null
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}