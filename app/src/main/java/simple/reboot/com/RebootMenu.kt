package simple.reboot.com

import android.os.Process
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import simple.reboot.com.actions.ActionItems
import simple.reboot.com.actions.PowerActionItem

@Composable
fun RebootMenu(
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel = viewModel(),
) {
    val uiState = viewModel.uiState

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        viewModel.areWeRooted()
    }

    if (uiState.areWeRooted == true) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .padding(all = 64.dp)
                        .clip(RoundedCornerShape(size = 16.dp))
                        .background(color = MaterialTheme.colorScheme.background)
                        .padding(16.dp),
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 16.dp)
                ) {
                    itemsIndexed(items = ActionItems.commands) { index, item ->
                        val bgColor = if (index == 0) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }

                        ActionButton(
                            actionItem = item,
                            bgColor = bgColor,
                            runCommand = { viewModel.runCommand(item.command) }
                        )
                    }
                }
            }
        }
    } else if (uiState.areWeRooted == false) {
        // we're dead in the water, warn the user and obliterate everything
        Toast.makeText(LocalContext.current, R.string.root_status_no, Toast.LENGTH_LONG).show()
        Process.killProcess(Process.myPid())
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    actionItem: PowerActionItem,
    bgColor: Color,
    runCommand: () -> Unit,
) {
    var width by remember { mutableIntStateOf(0) }
    val size = with(LocalDensity.current) { width.toDp() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { width = it.size.width },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(color = bgColor, shape = CircleShape)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(color = MaterialTheme.colorScheme.onBackground),
                    onClick = runCommand
                ),
            contentAlignment = Alignment.Center
        ) {
            if (actionItem.icon != null) {
                Icon(
                    painter = painterResource(id = actionItem.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(id = actionItem.title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}